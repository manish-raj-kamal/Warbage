const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true, index: true },
  name: { type: String, required: true, trim: true },
  email: { type: String, trim: true, lowercase: true },
  phone: { type: String, trim: true },
  role: { type: String, enum: ['COLLECTOR', 'RECYCLER'], required: true, index: true },

  // Recycler specific fields
  location: { type: String, trim: true },
  latitude: { type: Number },
  longitude: { type: Number },
  materialsHandled: { type: String, trim: true }, // e.g. "PCB, Cables"
  pricePerKg: { type: Number },
  authorized: { type: Boolean, default: false },

  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);
