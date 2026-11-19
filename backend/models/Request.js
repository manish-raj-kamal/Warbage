const mongoose = require('mongoose');

const requestSchema = new mongoose.Schema({
  requestId: { type: String, required: true, unique: true, index: true },
  lotId: { type: String, required: true, index: true },
  collectorId: { type: String, index: true },
  recyclerId: { type: String, required: true, index: true },
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
