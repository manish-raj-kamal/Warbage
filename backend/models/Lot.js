const mongoose = require('mongoose');

const lotSchema = new mongoose.Schema({
  lotId: { type: String, required: true, unique: true },
  collectorId: { type: String },
  materialId: { type: Number, required: true },
  weight: { type: Number, required: true },
  estimatedMinValue: { type: Number },
  estimatedMaxValue: { type: Number },
  description: { type: String },
  status: { type: String, enum: ['OPEN', 'ASSIGNED', 'COMPLETED', 'CANCELLED'], default: 'OPEN' },
  dateCreated: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Lot', lotSchema);