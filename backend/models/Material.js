const mongoose = require('mongoose');

const materialSchema = new mongoose.Schema({
  materialId: { type: Number, required: true, unique: true, index: true },
  name: { type: String, required: true, trim: true },
  description: { type: String, trim: true },
  category: { type: String, required: true, trim: true },
  recyclabilityScore: { type: Number, min: 0, max: 100 },
  averagePricePerKg: { type: Number, required: true },
  lastUpdated: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Material', materialSchema);
