const mongoose = require('mongoose');

const materialSchema = new mongoose.Schema({
  materialId: { type: Number, required: true, unique: true },
  name: { type: String, required: true },
  description: { type: String },
  category: { type: String, required: true },
  recyclabilityScore: { type: Number, min: 0, max: 100 },
  averagePricePerKg: { type: Number, required: true },
  lastUpdated: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Material', materialSchema);