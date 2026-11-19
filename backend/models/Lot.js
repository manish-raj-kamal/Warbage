const mongoose = require('mongoose');

const lotSchema = new mongoose.Schema({
  lotId: { type: String, required: true, unique: true, index: true },
  
  // Collector Information
  collectorId: { type: String, required: true, index: true },
  collectorName: { type: String, trim: true, default: '' },
  collectorEmail: { type: String, trim: true, default: '' },
  collectorPhone: { type: String, trim: true, default: '' },
  
  // Recycler Information
  recyclerId: { type: String, index: true, default: '' },
  recyclerName: { type: String, trim: true, default: '' },
  recyclerLocation: { type: String, trim: true, default: '' },
  recyclerPricePerKg: { type: Number, min: 0, default: 0 },

  // E-Waste Batch Data
  materialId: { type: Number, required: true },
  weight: { type: Number, required: true, min: 0 },
  estimatedMinValue: { type: Number, min: 0 },
  estimatedMaxValue: { type: Number, min: 0 },
  description: { type: String, trim: true },
  status: {
    type: String,
    uppercase: true,
    enum: ['OPEN', 'CREATED', 'SUBMITTED', 'ASSIGNED', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'],
    default: 'OPEN',
    index: true
  },
  dateCreated: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Lot', lotSchema);
