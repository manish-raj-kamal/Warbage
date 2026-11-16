import tensorflow as tf
import os
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input

# --- Local RTX 4050 Training Configuration ---
# We use the FULL Kaggle dataset directly since you are running locally without Colab limits!
DATASET_PATH_TRAIN = "data/train"
DATASET_PATH_VAL = "data/val"
IMG_SIZE = (224, 224)
BATCH_SIZE = 32
EPOCHS = 20

print("Checking GPUs...")
print(tf.config.list_physical_devices('GPU'))

# 1. Prepare Data Loaders using MobileNetV2's native preprocessor
train_datagen = ImageDataGenerator(
    preprocessing_function=preprocess_input, # Automatically scales to [-1, 1]
    rotation_range=30,
    width_shift_range=0.2,
    height_shift_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True,
    brightness_range=[0.8, 1.2]
)

val_datagen = ImageDataGenerator(preprocessing_function=preprocess_input)

print("Loading training dataset...")
train_generator = train_datagen.flow_from_directory(
    DATASET_PATH_TRAIN,
    target_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='categorical'
)

print("Loading validation dataset...")
val_generator = val_datagen.flow_from_directory(
    DATASET_PATH_VAL,
    target_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='categorical',
    shuffle=False
)

labels = list(train_generator.class_indices.keys())
NUM_CLASSES = len(labels)
print(f"Classes ({NUM_CLASSES}): {labels}")

# 2. Build a Bigger, Better Model (MobileNetV2)
# MobileNetV2 is much more capable of learning distinct shapes than V3Small.
base_model = MobileNetV2(weights='imagenet', include_top=False, input_shape=IMG_SIZE + (3,))
base_model.trainable = False

x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dropout(0.4)(x) # Help prevent overfitting on the large dataset
predictions = Dense(NUM_CLASSES, activation='softmax')(x)
model = Model(inputs=base_model.input, outputs=predictions)

# 3. Train Head (Phase 1)
print("Starting Phase 1 Training (Frozen Base)...")
model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=0.001), loss='categorical_crossentropy', metrics=['accuracy'])
model.fit(
    train_generator,
    epochs=10,
    validation_data=val_generator,
    callbacks=[tf.keras.callbacks.EarlyStopping(patience=3, restore_best_weights=True)]
)

# 4. Fine-Tune (Phase 2)
print("Starting Phase 2 Fine-Tuning (Unfreezing deeper layers)...")
base_model.trainable = True
# Unfreeze more layers this time since we have the full dataset
for layer in base_model.layers[:-50]:
    layer.trainable = False

model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5), loss='categorical_crossentropy', metrics=['accuracy'])
model.fit(
    train_generator,
    epochs=15,
    validation_data=val_generator,
    callbacks=[tf.keras.callbacks.EarlyStopping(patience=4, restore_best_weights=True)]
)

# 5. Export to TFLite
print("Converting to TensorFlow Lite...")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

with open("warbage_ewaste_model.tflite", 'wb') as f:
    f.write(tflite_model)

with open("labels.txt", "w") as f:
    for label in labels:
        f.write(label + "\n")

print("✅ DONE! The 'warbage_ewaste_model.tflite' model is ready for Android Studio.")