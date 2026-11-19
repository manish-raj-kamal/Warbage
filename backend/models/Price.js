const mongoose = require('mongoose');

const priceSchema = new mongoose.Schema({
  materialId: { type: Number, required: true, index: true },
  recyclerId: { type: String, required: true, index: true },
  pricePerKg: { type: Number, required: true },
  currency: { type: String, default: 'INR' },
  updatedAt: { type: Date, default: Date.now }
});

priceSchema.index({ materialId: 1, recyclerId: 1 }, { unique: true });

module.exports = mongoose.model('Price', priceSchema);
