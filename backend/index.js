const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const crypto = require('crypto');
const https = require('https');
require('dotenv').config();

const Material = require('./models/Material');
const Transaction = require('./models/Transaction');
const Price = require('./models/Price');
const User = require('./models/User');
const Lot = require('./models/Lot');
const Request = require('./models/Request');

const app = express();

// Security Response Headers
app.use((req, res, next) => {
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('X-XSS-Protection', '1; mode=block');
  res.setHeader('Strict-Transport-Security', 'max-age=31536000; includeSubDomains');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  next();
});

// Optional Helmet if installed
try {
  const helmet = require('helmet');
  app.use(helmet());
} catch (_) {
  // Graceful fallback to manual headers above
}

// Configurable CORS
const allowedOrigins = process.env.ALLOWED_ORIGINS 
  ? process.env.ALLOWED_ORIGINS.split(',').map(s => s.trim())
  : null;

app.use(cors({
  origin: (origin, callback) => {
    // Mobile apps and native clients do not send Origin header (allowed)
    if (!origin) return callback(null, true);
    if (!allowedOrigins || allowedOrigins.includes(origin)) {
      return callback(null, true);
    }
    return callback(new Error('CORS blocked: Origin not allowed.'));
  },
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-App-Key']
}));

app.use(express.json({ limit: '1mb' }));

// --- Serverless MongoDB Connection with Caching ---
let cachedDb = null;
async function connectToDatabase() {
  if (cachedDb && mongoose.connection.readyState === 1) {
    return cachedDb;
  }
  const uri = process.env.MONGODB_URI;
  if (!uri) {
    throw new Error('MONGODB_URI environment variable is missing.');
  }

  const conn = await mongoose.connect(uri, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    serverSelectionTimeoutMS: 5000,
    maxPoolSize: 10
  });
  cachedDb = conn;
  return conn;
}

// Ensure DB connected on each request
app.use(async (req, res, next) => {
  try {
    await connectToDatabase();
    next();
  } catch (err) {
    console.error('Database connection failure:', err.message);
    res.status(503).json({ error: 'Database service unavailable. Please try again.' });
  }
});

// --- Google Firebase Public Certificate Cache for Token Verification ---
const FIREBASE_PROJECT_ID = process.env.FIREBASE_PROJECT_ID || 'warbage-c6edc';
const WARBAGE_APP_KEY = process.env.WARBAGE_APP_KEY || 'warbage-android-v1';
const ALLOW_DEMO = process.env.ALLOW_DEMO_ACCOUNTS !== 'false';

let publicKeysCache = null;
let publicKeysExpiresAt = 0;

function fetchGooglePublicKeys() {
  return new Promise((resolve, reject) => {
    const now = Date.now();
    if (publicKeysCache && now < publicKeysExpiresAt) {
      return resolve(publicKeysCache);
    }

    const url = 'https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com';
    https.get(url, (res) => {
      let data = '';
      res.on('data', chunk => { data += chunk; });
      res.on('end', () => {
        try {
          if (res.statusCode !== 200) {
            return reject(new Error(`Failed to fetch Google certs: HTTP ${res.statusCode}`));
          }
          publicKeysCache = JSON.parse(data);
          
          // Cache max-age from headers or default to 6 hours
          const cacheControl = res.headers['cache-control'] || '';
          const maxAgeMatch = cacheControl.match(/max-age=(\d+)/);
          const maxAgeSec = maxAgeMatch ? parseInt(maxAgeMatch[1], 10) : 21600;
          publicKeysExpiresAt = now + (maxAgeSec * 1000);
          resolve(publicKeysCache);
        } catch (e) {
          reject(e);
        }
      });
    }).on('error', reject);
  });
}

/**
 * Verify Firebase ID Token cryptographically (RS256 with Google certs).
 */
async function verifyFirebaseToken(token) {
  const parts = token.split('.');
  if (parts.length !== 3) {
    throw new Error('Invalid JWT structure');
  }

  const [headerB64, payloadB64, signatureB64] = parts;
  const header = JSON.parse(Buffer.from(headerB64, 'base64').toString('utf8'));
  const payload = JSON.parse(Buffer.from(payloadB64, 'base64').toString('utf8'));

  if (header.alg !== 'RS256') {
    throw new Error('Unsupported JWT algorithm: ' + header.alg);
  }

  const nowSec = Math.floor(Date.now() / 1000);
  if (payload.exp && payload.exp < nowSec) {
    throw new Error('Firebase token expired');
  }

  const expectedIssuer = `https://securetoken.google.com/${FIREBASE_PROJECT_ID}`;
  if (payload.iss !== expectedIssuer) {
    throw new Error(`Invalid token issuer: ${payload.iss}`);
  }

  if (payload.aud !== FIREBASE_PROJECT_ID) {
    throw new Error(`Invalid token audience: ${payload.aud}`);
  }

  if (!payload.sub || typeof payload.sub !== 'string') {
    throw new Error('Invalid token subject');
  }

  // Verify signature against Google Public Keys
  const certs = await fetchGooglePublicKeys();
  const cert = certs[header.kid];
  if (!cert) {
    throw new Error(`Google certificate key id ${header.kid} not found`);
  }

  const verifier = crypto.createVerify('RSA-SHA256');
  verifier.update(`${headerB64}.${payloadB64}`);
  const signature = Buffer.from(signatureB64, 'base64url');

  const isValid = verifier.verify(cert, signature);
  if (!isValid) {
    throw new Error('JWT signature verification failed');
  }

  return {
    uid: payload.sub,
    email: payload.email || '',
    name: payload.name || '',
    authProvider: payload.firebase?.sign_in_provider || 'firebase'
  };
}

/**
 * Authentication Middleware:
 * Validates Firebase Token, Demo Token, or Client Secret.
 */
async function requireAuth(req, res, next) {
  const authHeader = req.headers.authorization;
  const clientAppKey = req.headers['x-app-key'];

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    // If request has valid app secret key but no user, allow internal app health check / public catalog only
    return res.status(401).json({ error: 'Unauthorized: Missing or invalid Authorization Bearer token.' });
  }

  const token = authHeader.substring(7).trim();

  // 1. Check Demo Accounts (for Play Store testing / offline test mode)
  if (token.startsWith('demo:')) {
    if (!ALLOW_DEMO) {
      return res.status(403).json({ error: 'Demo accounts are disabled in this environment.' });
    }
    const demoUid = token.substring(5).trim();
    if (!demoUid) {
      return res.status(401).json({ error: 'Invalid demo user id.' });
    }
    req.user = {
      uid: demoUid,
      email: `${demoUid}@warbage.eco`,
      isDemo: true
    };
    return next();
  }

  // 2. Check Firebase ID Token
  try {
    const decoded = await verifyFirebaseToken(token);
    req.user = decoded;
    return next();
  } catch (err) {
    // Fallback: If token couldn't be validated against Google certs due to network, check client key
    if (clientAppKey === WARBAGE_APP_KEY && process.env.NODE_ENV !== 'production') {
      try {
        const payload = JSON.parse(Buffer.from(token.split('.')[1], 'base64').toString('utf8'));
        if (payload.sub) {
          req.user = { uid: payload.sub, email: payload.email || '', isDevFallback: true };
          return next();
        }
      } catch (_) {}
    }

    console.warn('Auth token verification failed:', err.message);
    return res.status(401).json({ error: `Unauthorized: Authentication failed (${err.message}).` });
  }
}

// Health Check
app.get('/', (req, res) => {
  res.json({
    status: 'online',
    service: 'Warbage Eco Backend API',
    version: '1.1.0'
  });
});

// --- Materials API (Public Read, Protected Write) ---
app.get('/api/materials', async (req, res) => {
  try {
    const materials = await Material.find().select('-__v');
    res.json(materials);
  } catch (error) {
    res.status(500).json({ error: 'Unable to fetch materials.' });
  }
});

app.post('/api/materials', requireAuth, async (req, res) => {
  try {
    // Only authorized recyclers or app admin can create materials
    const user = await User.findOne({ userId: req.user.uid });
    if (!user || user.role !== 'RECYCLER' || !user.authorized) {
      return res.status(403).json({ error: 'Forbidden: Only authorized recyclers may add materials.' });
    }
    const material = new Material(req.body);
    await material.save();
    res.status(201).json(material);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Prices API (Public Read, Protected Write) ---
app.get('/api/prices', async (req, res) => {
  try {
    const prices = await Price.find().select('-__v');
    res.json(prices);
  } catch (error) {
    res.status(500).json({ error: 'Unable to fetch price board.' });
  }
});

app.post('/api/prices', requireAuth, async (req, res) => {
  try {
    const user = await User.findOne({ userId: req.user.uid });
    if (!user || user.role !== 'RECYCLER') {
      return res.status(403).json({ error: 'Forbidden: Only recyclers may submit prices.' });
    }
    const priceData = {
      ...req.body,
      recyclerId: req.user.uid
    };
    const price = new Price(priceData);
    await price.save();
    res.status(201).json(price);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Users API (Strict Data Isolation & Privacy) ---
app.get('/api/users', requireAuth, async (req, res) => {
  try {
    // Case 1: Directory lookup for recyclers (public map / recycler list)
    if (req.query.role === 'RECYCLER') {
      // ONLY return public fields needed for discovery; NEVER return emails or phone numbers
      const recyclers = await User.find({ role: 'RECYCLER' })
        .select('userId name location latitude longitude materialsHandled pricePerKg authorized -_id');
      return res.json(recyclers);
    }

    // Case 2: User querying their own profile
    if (req.query.userId) {
      if (req.query.userId !== req.user.uid) {
        return res.status(403).json({ error: 'Forbidden: You may only access your own profile.' });
      }
      const user = await User.findOne({ userId: req.query.userId }).select('-__v -_id');
      return res.json(user ? [user] : []);
    }

    // Prohibit dumping all users
    return res.status(403).json({ error: 'Forbidden: Unauthorized user directory query.' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to retrieve user data.' });
  }
});

app.post('/api/users', requireAuth, async (req, res) => {
  try {
    const authenticatedUid = req.user.uid;
    const bodyUserId = req.body.userId;

    // Strict validation: user can only upsert their OWN profile
    if (bodyUserId && bodyUserId !== authenticatedUid) {
      return res.status(403).json({ error: 'Forbidden: Cannot create or modify profile for another user ID.' });
    }

    // Sanitize payload: users cannot grant themselves 'authorized' status
    const updateData = {
      ...req.body,
      userId: authenticatedUid,
      email: req.user.email || req.body.email
    };
    delete updateData.authorized; // Protected field

    const user = await User.findOneAndUpdate(
      { userId: authenticatedUid },
      { $set: updateData },
      { new: true, upsert: true, runValidators: true }
    ).select('-__v');

    res.status(201).json(user);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Account Deletion Endpoint (Google Play & Privacy Law Requirement)
app.delete('/api/users/:id', requireAuth, async (req, res) => {
  try {
    const targetId = req.params.id;
    if (targetId !== req.user.uid) {
      return res.status(403).json({ error: 'Forbidden: You may only delete your own account.' });
    }

    // 1. Delete user profile
    await User.findOneAndDelete({ userId: targetId });

    // 2. Remove open lots created by this user
    await Lot.deleteMany({ collectorId: targetId, status: 'OPEN' });

    // 3. Cancel pending pickup requests
    await Request.updateMany(
      { recyclerId: targetId, status: 'PENDING' },
      { status: 'REJECTED', updatedAt: Date.now() }
    );

    res.json({ success: true, message: 'Account and associated data successfully deleted.' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to process account deletion.' });
  }
});

// --- Lots API (Ownership & Role Scoped) ---
app.get('/api/lots', requireAuth, async (req, res) => {
  try {
    const filter = {};
    
    // Check user's role in DB
    const user = await User.findOne({ userId: req.user.uid });
    const isRecycler = user?.role === 'RECYCLER';

    if (!isRecycler) {
      // Collectors can strictly only see their own lots
      filter.collectorId = req.user.uid;
    } else {
      // Recyclers can view open lots or lots assigned to them
      if (req.query.status) {
        filter.status = req.query.status;
      } else {
        filter.status = { $in: ['OPEN', 'ASSIGNED'] };
      }
    }

    const lots = await Lot.find(filter).sort({ dateCreated: -1 }).select('-__v');
    res.json(lots);
  } catch (error) {
    res.status(500).json({ error: 'Unable to retrieve lots.' });
  }
});

app.post('/api/lots', requireAuth, async (req, res) => {
  try {
    const lotData = {
      ...req.body,
      collectorId: req.user.uid // Force ownership to authenticated user
    };
    const lot = new Lot(lotData);
    await lot.save();
    res.status(201).json(lot);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

app.put('/api/lots/:id/status', requireAuth, async (req, res) => {
  try {
    const existing = await Lot.findOne({ lotId: req.params.id });
    if (!existing) {
      return res.status(404).json({ error: 'Lot not found.' });
    }

    const user = await User.findOne({ userId: req.user.uid });
    const isOwner = existing.collectorId === req.user.uid;
    const isRecycler = user?.role === 'RECYCLER';

    if (!isOwner && !isRecycler) {
      return res.status(403).json({ error: 'Forbidden: You do not have permission to update this lot.' });
    }

    existing.status = req.body.status;
    await existing.save();
    res.json(existing);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Requests API (Authenticated & Scoped) ---
app.get('/api/requests', requireAuth, async (req, res) => {
  try {
    // Only return requests where the user is either the recycler or collector
    const userLots = await Lot.find({ collectorId: req.user.uid }).distinct('lotId');
    const filter = {
      $or: [
        { recyclerId: req.user.uid },
        { lotId: { $in: userLots } }
      ]
    };

    const requests = await Request.find(filter).sort({ dateCreated: -1 }).select('-__v');
    res.json(requests);
  } catch (error) {
    res.status(500).json({ error: 'Unable to retrieve requests.' });
  }
});

app.post('/api/requests', requireAuth, async (req, res) => {
  try {
    const requestData = {
      ...req.body,
      recyclerId: req.user.uid // Enforce caller identity
    };
    const request = new Request(requestData);
    await request.save();
    res.status(201).json(request);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

app.put('/api/requests/:id/status', requireAuth, async (req, res) => {
  try {
    const existing = await Request.findOne({ requestId: req.params.id });
    if (!existing) {
      return res.status(404).json({ error: 'Request not found.' });
    }

    // Must be either the recycler who made the request or the collector owning the lot
    const linkedLot = await Lot.findOne({ lotId: existing.lotId });
    const isRecycler = existing.recyclerId === req.user.uid;
    const isCollector = linkedLot && linkedLot.collectorId === req.user.uid;

    if (!isRecycler && !isCollector) {
      return res.status(403).json({ error: 'Forbidden: You are not authorized to update this request.' });
    }

    existing.status = req.body.status;
    existing.updatedAt = Date.now();
    await existing.save();
    res.json(existing);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Transactions API (Scoped to Participating Parties) ---
app.get('/api/transactions', requireAuth, async (req, res) => {
  try {
    // Strictly isolate financial transaction records
    const filter = {
      $or: [
        { collectorId: req.user.uid },
        { recyclerId: req.user.uid }
      ]
    };
    const transactions = await Transaction.find(filter).sort({ transactionDate: -1 }).select('-__v');
    res.json(transactions);
  } catch (error) {
    res.status(500).json({ error: 'Unable to fetch transactions.' });
  }
});

app.post('/api/transactions', requireAuth, async (req, res) => {
  try {
    const { collectorId, recyclerId } = req.body;
    if (collectorId !== req.user.uid && recyclerId !== req.user.uid) {
      return res.status(403).json({ error: 'Forbidden: You cannot record transactions for other users.' });
    }
    const transaction = new Transaction(req.body);
    await transaction.save();
    res.status(201).json(transaction);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

app.put('/api/transactions/:id/status', requireAuth, async (req, res) => {
  try {
    const transaction = await Transaction.findById(req.params.id);
    if (!transaction) {
      return res.status(404).json({ error: 'Transaction not found.' });
    }

    if (transaction.collectorId !== req.user.uid && transaction.recyclerId !== req.user.uid) {
      return res.status(403).json({ error: 'Forbidden: You are not a party to this transaction.' });
    }

    transaction.status = req.body.status;
    await transaction.save();
    res.json(transaction);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Global 404 Handler for Unrecognized Endpoints
app.use((req, res) => {
  res.status(404).json({ error: 'Endpoint not found.' });
});

// Global Error Handler (Sanitizes Error Details)
app.use((err, req, res, next) => {
  console.error('Unhandled server error:', err);
  res.status(500).json({ error: 'An unexpected internal error occurred.' });
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Secured Warbage Server running on port ${PORT}`);
});

module.exports = app;
