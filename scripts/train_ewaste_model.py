import tensorflow as tf
import os
import shutil
import random
from tensorflow.keras.applications import MobileNetV3Small
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing.image import ImageDataGenerator

# 18-Class Configuration
DATASET_PATH_TRAIN = "data/train"
DATASET_PATH_VAL = "data/val"
IMG_SIZE = (224, 224)
BATCH_SIZE = 32
MAX_IMAGES_PER_CLASS = 250 # Capping to prevent timeouts!

def cap_dataset(source_dir, dest_dir, max_images):
    print(f"Capping dataset from {source_dir} to {dest_dir} (Max {max_images} imgs/class)...")
    if os.path.exists(dest_dir):
        shutil.rmtree(dest_dir)
    os.makedirs(dest_dir)

    classes = os.listdir(source_dir)
    for cls in classes:
        cls_path = os.path.join(source_dir, cls)
        if not os.path.isdir(cls_path): continue

        dest_cls_path = os.path.join(dest_dir, cls)
        os.makedirs(dest_cls_path, exist_ok=True)

        files = os.listdir(cls_path)
        random.shuffle(files) # Shuffle so we get a diverse mix

        for img_name in files[:max_images]:
            shutil.copy(os.path.join(cls_path, img_name), os.path.join(dest_cls_path, img_name))

capped_train_dir = "warbage_capped_train"
capped_val_dir = "warbage_capped_val"
cap_dataset(DATASET_PATH_TRAIN, capped_train_dir, MAX_IMAGES_PER_CLASS)
cap_dataset(DATASET_PATH_VAL, capped_val_dir, int(MAX_IMAGES_PER_CLASS * 0.2)) # 20% for val

# --- 2. Prepare Data Loaders ---
# IMPORTANT FIX: MobileNetV3 internally expects [0, 255] and rescales it to [-1, 1].
# If we pass rescale=1./255, the model sees everything as black!
train_datagen = ImageDataGenerator(
    # NO RESCALE HERE!
    rotation_range=30,
    width_shift_range=0.2,
    height_shift_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True,
    brightness_range=[0.8, 1.2]
)
val_datagen = ImageDataGenerator() # NO RESCALE HERE!

print("Loading training dataset...")
train_generator = train_datagen.flow_from_directory(
    capped_train_dir, target_size=IMG_SIZE, batch_size=BATCH_SIZE, class_mode='categorical'
)

print("Loading validation dataset...")
val_generator = val_datagen.flow_from_directory(
    capped_val_dir, target_size=IMG_SIZE, batch_size=BATCH_SIZE, class_mode='categorical', shuffle=False
)

labels = list(train_generator.class_indices.keys())
NUM_CLASSES = len(labels)
print(f"Classes ({NUM_CLASSES}): {labels}")

# --- 3. Build Model ---
base_model = MobileNetV3Small(weights='imagenet', include_top=False, input_shape=IMG_SIZE + (3,))
base_model.trainable = False

x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dropout(0.3)(x)
predictions = Dense(NUM_CLASSES, activation='softmax')(x)
model = Model(inputs=base_model.input, outputs=predictions)

# --- 4. Train Head (Phase 1) ---
print("Starting Phase 1 Training...")
model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=0.001), loss='categorical_crossentropy', metrics=['accuracy'])
model.fit(
    train_generator, epochs=10, validation_data=val_generator,
    callbacks=[tf.keras.callbacks.EarlyStopping(patience=3, restore_best_weights=True)]
)

# --- 5. Fine-Tune (Phase 2) ---
print("Starting Phase 2 Fine-Tuning...")
base_model.trainable = True
for layer in base_model.layers[:-30]:
    layer.trainable = False

model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5), loss='categorical_crossentropy', metrics=['accuracy'])
model.fit(
    train_generator, epochs=10, validation_data=val_generator,
    callbacks=[tf.keras.callbacks.EarlyStopping(patience=3, restore_best_weights=True)]
)

# --- 6. Export ---
print("Converting to TensorFlow Lite...")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

with open("warbage_ewaste_model.tflite", 'wb') as f:
    f.write(tflite_model)
with open("labels.txt", "w") as f:
    for label in labels:
        f.write(label + "\n")

print("✅ DONE! Download 'warbage_ewaste_model.tflite' and 'labels.txt' from the left menu.")