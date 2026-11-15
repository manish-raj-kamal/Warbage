import tensorflow as tf
import os
import shutil
import random
from tensorflow.keras.applications import MobileNetV3Small
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing.image import ImageDataGenerator

# Configuration
DATASET_PATH_TRAIN = "data/train"
DATASET_PATH_VAL = "data/val"
TARGET_CATEGORIES = ["PCB", "Cables", "Batteries", "LCD_Panels", "Other"]
IMG_SIZE = (224, 224)
BATCH_SIZE = 32
NUM_CLASSES = 5
EPOCHS = 20

# Max images per class to prevent memory issues and keep the training fast
MAX_IMAGES_PER_CLASS = 150

def reorganize_and_cap_dataset(source_dir, dest_dir, max_images):
    print(f"Reorganizing and capping dataset from {source_dir} to {dest_dir} (max {max_images} imgs/class)...")
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)

    for cat in TARGET_CATEGORIES:
        os.makedirs(os.path.join(dest_dir, cat), exist_ok=True)

    category_mapping = {
        "PCB": "PCB", "Microchip-IC": "PCB", "transistor": "PCB",
        "Resistor": "PCB", "Passive-Components": "PCB",
        "Battery": "Batteries",
        "Laptop": "LCD_Panels", "Television": "LCD_Panels", "Mobile": "LCD_Panels",
        "Air-Conditioner": "Other", "Keyboard": "Other", "Microwave": "Other",
        "Mouse": "Other", "Printer": "Other", "Refrigerator": "Other",
        "Washing Machine": "Other", "heat-sink": "Other", "light bulbs": "Other"
    }

    # Keep track of how many images we've copied for each TARGET category
    copied_counts = {cat: 0 for cat in TARGET_CATEGORIES}

    if os.path.exists(source_dir):
        # We process folders, but we want to shuffle the files so we don't just get one type
        for original_class in os.listdir(source_dir):
            original_path = os.path.join(source_dir, original_class)
            if not os.path.isdir(original_path): continue

            target_class = category_mapping.get(original_class, "Other")

            # Get all files and shuffle them
            files = os.listdir(original_path)
            random.shuffle(files)

            for img_name in files:
                if copied_counts[target_class] >= max_images:
                    break # Stop copying for this target class if we hit the limit

                src_file = os.path.join(original_path, img_name)
                dst_file = os.path.join(dest_dir, target_class, f"{original_class}_{img_name}")

                if not os.path.exists(dst_file):
                    shutil.copy(src_file, dst_file)
                    copied_counts[target_class] += 1

    print(f"Final counts in {dest_dir}: {copied_counts}")

# Create the mapped dataset directories, capping at our limit
mapped_train_dir = "warbage_mapped_train"
mapped_val_dir = "warbage_mapped_val"
reorganize_and_cap_dataset(DATASET_PATH_TRAIN, mapped_train_dir, MAX_IMAGES_PER_CLASS)
# For validation, we need even fewer (e.g. 20% of max)
reorganize_and_cap_dataset(DATASET_PATH_VAL, mapped_val_dir, int(MAX_IMAGES_PER_CLASS * 0.2))

# 1. Prepare Data Loaders
train_datagen = ImageDataGenerator(
    rescale=1./255,
    rotation_range=20,
    width_shift_range=0.2,
    height_shift_range=0.2,
    horizontal_flip=True,
    brightness_range=[0.8, 1.2]
)
val_datagen = ImageDataGenerator(rescale=1./255)

print("Loading training dataset...")
train_generator = train_datagen.flow_from_directory(
    mapped_train_dir,
    target_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='categorical'
)

print("Loading validation dataset...")
val_generator = val_datagen.flow_from_directory(
    mapped_val_dir,
    target_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='categorical',
    shuffle=False
)

labels = list(train_generator.class_indices.keys())
print(f"Classes: {labels}")

# 2. Load Pretrained MobileNetV3Small
base_model = MobileNetV3Small(weights='imagenet', include_top=False, input_shape=IMG_SIZE + (3,))
base_model.trainable = False

# 3. Add Custom Classification Head
x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dropout(0.2)(x)
predictions = Dense(NUM_CLASSES, activation='softmax')(x)

model = Model(inputs=base_model.input, outputs=predictions)

# 4. Compile Model
model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
    loss='categorical_crossentropy',
    metrics=['accuracy', tf.keras.metrics.Precision(), tf.keras.metrics.Recall()]
)

# 5. Train Model
print("Starting training...")
history = model.fit(
    train_generator,
    epochs=EPOCHS,
    validation_data=val_generator,
    callbacks=[
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True),
        tf.keras.callbacks.ModelCheckpoint('best_model.keras', save_best_only=True)
    ]
)

# 6. Evaluate Model
print("Evaluating model...")
loss, accuracy, precision, recall = model.evaluate(val_generator)
print(f"Validation Accuracy: {accuracy*100:.2f}%")

# (Optional) Fine-Tuning
print("Unfreezing upper layers for fine-tuning...")
base_model.trainable = True
for layer in base_model.layers[:-20]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5),
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(
    train_generator,
    epochs=5,
    validation_data=val_generator,
    callbacks=[
        tf.keras.callbacks.EarlyStopping(patience=3, restore_best_weights=True)
    ]
)

# 7. Convert and Export to TensorFlow Lite
print("Converting to TensorFlow Lite...")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

tflite_model_path = "warbage_ewaste_model.tflite"
with open(tflite_model_path, 'wb') as f:
    f.write(tflite_model)
print(f"Saved TFLite model to {tflite_model_path}")

with open("labels.txt", "w") as f:
    for label in labels:
        f.write(label + "\n")
print("Saved labels.txt")