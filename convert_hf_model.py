import tensorflow as tf

model_path = 'app/src/main/assets/e_waste_classifier.keras'
print(f"Loading model from {model_path}...")
model = tf.keras.models.load_model(model_path)

print("Input shape:", model.input_shape)

print("Converting to TFLite...")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

tflite_path = 'app/src/main/assets/e_waste_classifier.tflite'
with open(tflite_path, 'wb') as f:
    f.write(tflite_model)
print(f"Saved TFLite model to {tflite_path}")
