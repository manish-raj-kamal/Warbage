const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
  transactionId: { type: String, unique: true, sparse: true, index: true },
  lotId: { type: String, index: true },
  requestId: { type: String, index: true },
  collectorId: { type: String, required: true, index: true },
  recyclerId: { type: String, required: true, index: true },
  materialId: { type: Number, required: true },
  weightKg: { type: Number, required: true, min: 0 },
  totalPrice: { type: Number, required: true, min: 0 },
  status: {
    type: String,
    uppercase: true,
    enum: ['PENDING', 'ACCEPTED', 'COMPLETED', 'CANCELLED'],
    default: 'COMPLETED',
    index: true
  },
  transactionDate: { type: Date, default: Date.now },
  notes: { type: String, trim: true }
});

module.exports = mongoose.model('Transaction', transactionSchema);
