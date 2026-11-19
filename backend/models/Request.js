const mongoose = require('mongoose');

const requestSchema = new mongoose.Schema({
  requestId: { type: String, required: true, unique: true, index: true },
  lotId: { type: String, required: true, index: true },
  
  // Collector Information
  collectorId: { type: String, index: true },
  collectorName: { type: String, trim: true, default: '' },
  collectorPhone: { type: String, trim: true, default: '' },
  
  // Recycler Information
  recyclerId: { type: String, required: true, index: true },
  recyclerName: { type: String, trim: true, default: '' },
  recyclerLocation: { type: String, trim: true, default: '' },
  
  status: {
    type: String,
    uppercase: true,
    enum: ['PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'],
    default: 'PENDING',
    index: true
  },
  dateCreated: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Request', requestSchema);
