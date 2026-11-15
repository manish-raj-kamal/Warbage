const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
  collectorId: { type: String, required: true },
  recyclerId: { type: String, required: true },
  materialId: { type: Number, required: true },
  weightKg: { type: Number, required: true },
  totalPrice: { type: Number, required: true },
  status: { type: String, enum: ['PENDING', 'ACCEPTED', 'COMPLETED', 'CANCELLED'], default: 'PENDING' },
  transactionDate: { type: Date, default: Date.now },
  notes: { type: String }
});

module.exports = mongoose.model('Transaction', transactionSchema);