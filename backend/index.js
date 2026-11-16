const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const Material = require('./models/Material');
const Transaction = require('./models/Transaction');
const Price = require('./models/Price');
const User = require('./models/User');
const Lot = require('./models/Lot');
const Request = require('./models/Request');

const app = express();
app.use(cors());
app.use(express.json());

// Connect to MongoDB
mongoose.connect(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
}).then(() => console.log("MongoDB Connected"))
  .catch(err => console.error("MongoDB Connection Error:", err));

app.get('/', (req, res) => {
  res.send('Warbage API is running!');
});

// --- Materials API ---
app.get('/api/materials', async (req, res) => {
  try {
    const materials = await Material.find();
    res.json(materials);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/materials', async (req, res) => {
  try {
    const material = new Material(req.body);
    await material.save();
    res.status(201).json(material);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Transactions API ---
app.get('/api/transactions', async (req, res) => {
  try {
    const filter = {};
    if (req.query.collectorId) filter.collectorId = req.query.collectorId;
    if (req.query.recyclerId) filter.recyclerId = req.query.recyclerId;

    const transactions = await Transaction.find(filter).sort({ transactionDate: -1 });
    res.json(transactions);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/transactions', async (req, res) => {
  try {
    const transaction = new Transaction(req.body);
    await transaction.save();
    res.status(201).json(transaction);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Users API ---
app.get('/api/users', async (req, res) => {
  try {
    const filter = {};
    if (req.query.role) filter.role = req.query.role;
    const users = await User.find(filter);
    res.json(users);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/users', async (req, res) => {
  try {
    const { userId } = req.body;
    // Upsert user to avoid duplicates if they login again
    const user = await User.findOneAndUpdate(
      { userId: userId },
      req.body,
      { new: true, upsert: true }
    );
    res.status(201).json(user);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Lots API ---
app.get('/api/lots', async (req, res) => {
  try {
    const filter = {};
    if (req.query.collectorId) filter.collectorId = req.query.collectorId;
    if (req.query.status) filter.status = req.query.status;
    const lots = await Lot.find(filter).sort({ dateCreated: -1 });
    res.json(lots);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/lots', async (req, res) => {
  try {
    const lot = new Lot(req.body);
    await lot.save();
    res.status(201).json(lot);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

app.put('/api/lots/:id/status', async (req, res) => {
  try {
    const lot = await Lot.findOneAndUpdate(
      { lotId: req.params.id },
      { status: req.body.status },
      { new: true }
    );
    res.json(lot);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Requests API ---
app.get('/api/requests', async (req, res) => {
  try {
    const filter = {};
    if (req.query.recyclerId) filter.recyclerId = req.query.recyclerId;
    if (req.query.lotId) filter.lotId = req.query.lotId;
    const requests = await Request.find(filter).sort({ dateCreated: -1 });
    res.json(requests);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/requests', async (req, res) => {
  try {
    const request = new Request(req.body);
    await request.save();
    res.status(201).json(request);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

app.put('/api/requests/:id/status', async (req, res) => {
  try {
    const request = await Request.findOneAndUpdate(
      { requestId: req.params.id },
      { status: req.body.status, updatedAt: Date.now() },
      { new: true }
    );
    res.json(request);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

app.put('/api/transactions/:id/status', async (req, res) => {
  try {
    const transaction = await Transaction.findByIdAndUpdate(
      req.params.id,
      { status: req.body.status },
      { new: true }
    );
    res.json(transaction);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// --- Prices API ---
app.get('/api/prices', async (req, res) => {
  try {
    const prices = await Price.find();
    res.json(prices);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/prices', async (req, res) => {
  try {
    const price = new Price(req.body);
    await price.save();
    res.status(201).json(price);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Start Server (For local dev; Vercel handles this automatically)
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});

module.exports = app;