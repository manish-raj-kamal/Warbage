import tensorflow as tf
model_path = 'app/src/main/assets/e_waste_classifier.keras'
model = tf.keras.models.load_model(model_path, compile=False)
print("Input shape:", model.input_shape)
print("Layers:")
for i, layer in enumerate(model.layers[:5]):
    print(f"  {i}: {layer.name} - {layer.__class__.__name__}")
