const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  userId: { type: Number, required: true, unique: true },
  name: { type: String, required: true },
  email: { type: String },
  phone: { type: String },
  role: { type: String, enum: ['COLLECTOR', 'RECYCLER'], required: true },

  // Recycler specific fields
  location: { type: String },
  latitude: { type: Number },
  longitude: { type: Number },
  materialsHandled: { type: String }, // e.g. "PCB, Cables"
  pricePerKg: { type: Number },
  authorized: { type: Boolean, default: false },

  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);