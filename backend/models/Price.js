const mongoose = require('mongoose');

const priceSchema = new mongoose.Schema({
  materialId: { type: Number, required: true },
  recyclerId: { type: String, required: true },
  pricePerKg: { type: Number, required: true },
  currency: { type: String, default: 'INR' },
  updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Price', priceSchema);