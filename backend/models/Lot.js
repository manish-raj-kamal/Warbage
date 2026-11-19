const mongoose = require('mongoose');

const lotSchema = new mongoose.Schema({
  lotId: { type: String, required: true, unique: true, index: true },
  collectorId: { type: String, required: true, index: true },
  materialId: { type: Number, required: true },
  weight: { type: Number, required: true, min: 0 },
  estimatedMinValue: { type: Number, min: 0 },
  estimatedMaxValue: { type: Number, min: 0 },
  description: { type: String, trim: true },
  status: { type: String, enum: ['OPEN', 'ASSIGNED', 'COMPLETED', 'CANCELLED'], default: 'OPEN', index: true },
  dateCreated: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Lot', lotSchema);
