const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const Material = require('./models/Material');
const Transaction = require('./models/Transaction');
const Price = require('./models/Price');

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