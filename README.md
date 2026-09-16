# Warbage — Smart E-Waste Recycling & Management Platform

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![UI Toolkit](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Minimum SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-lightgrey.svg)](https://developer.android.com/)

**Warbage** is an Android application designed to bridge the gap between e-waste generators (households, offices, individuals) and certified e-waste recyclers. It streamlines the lifecycle of electronic scrap: from identifying materials and estimating fair market scrap values, to locating nearby authorized recyclers on an interactive map, creating pickup lots, and tracking transaction statuses in real time.

---

## 📱 Features

### User Experience (E-Waste Generator)
- **Dashboard & Price Board**: Live estimated market scrap rates (₹/kg) across major e-waste categories (PCBs, Batteries, LCD/LED Panels, Motors, Cables, CRTs, Mixed Plastics).
- **Interactive Scrap Calculator**: Weight input with instant value estimation and dynamic lot creation.
- **AI Material Scanner (CameraX)**: Real-time camera viewfinder that analyzes e-waste items and suggests material classification and confidence ratings.
- **Authorized Recycler Directory**: Filtered list of verified recyclers with accepted material badges, operating hours, and contact information.
- **Interactive Google Maps**: Visualizes recyclers relative to the user's real-time GPS coordinates with distance calculations (Haversine formula).
- **Lot & Pickup Request Submission**: Bundle collected e-waste into trackable pickup/handover requests.
- **Transaction History**: Real-time status tracking (`PENDING`, `ACCEPTED`, `COMPLETED`, `REJECTED`) backed by local reactive flows.

### Recycler Experience
- **Recycler Portal**: Switch to recycler view to manage incoming pickup requests.
- **Request Management**: Inspect lot details (weights, estimated values, submission timestamps) and update request states with immediate visual feedback.

### Architecture & System Services
- **Local Persistence (Room DB)**: Offline-first architecture with pre-populated material rates and verified recycler facilities.
- **Background Sync (WorkManager)**: `PeriodicWorkRequest` running background sync workers to ensure transaction consistency and data freshness.
- **System Notifications**: Status update alerts for pickup confirmations and state transitions.

---

## 🛠️ Tech Stack & Dependencies

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.2.10 |
| **Build System** | Gradle with Version Catalog (`libs.versions.toml`) |
| **Android Gradle Plugin (AGP)** | 9.4.0 (Compatible with Android Studio Ladybug/Iguana+) |
| **UI Toolkit** | Jetpack Compose (BOM 2026.02.01) + Material 3 |
| **Navigation** | Jetpack Navigation Compose 2.9.0 |
| **Database** | Room SQLite 2.7.1 with KSP code generation |
| **Background Processing** | AndroidX WorkManager 2.11.0 |
| **Camera & Vision** | CameraX 1.5.0 (Core, Lifecycle, Camera2, View) |
| **Maps & Location** | Google Maps SDK 19.1.0, Maps Compose 6.4.1, Play Services Location 21.3.0 |
| **Asynchronous Programming** | Kotlin Coroutines & Reactive Flows |

---

## 💻 Prerequisites & Setup

To clone and run this project seamlessly in **Android Studio**:

1. **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer.
2. **Java Development Kit (JDK)**: JDK 17 or JDK 21 (bundled with recent Android Studio versions).
   - In Android Studio: `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle` → set **Gradle JDK** to version 17 or 21.
3. **Android SDK**:
   - `compileSdk`: 35 (or 37 preview)
   - `minSdk`: 24 (Android 7.0 Nougat)
   - `targetSdk`: 35 / 37
4. **Google Play Services**: Required on the emulator or physical device for Google Maps and Location.

---

## 🚀 Cloning & Opening the Project

### 1. Clone the Repository
```bash
git clone https://github.com/manish-raj-kamal/Warbage.git
cd Warbage
```

### 2. Open in Android Studio
1. Open **Android Studio**.
2. Select **File > Open...** (or click **Open** on the Welcome screen).
3. Navigate to the cloned `Warbage` folder and click **OK**.
4. Allow Gradle to perform the initial sync. Gradle will download all necessary dependencies specified in `gradle/libs.versions.toml`.

### 3. Build the Project
You can build the project from the terminal or using Android Studio's **Build > Make Project** (`Ctrl + F9` / `Cmd + F9`):
```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

---

## 🔑 Google Maps API Key Configuration

The app includes Google Maps integration (`RecyclerMapScreen.kt`) to plot recyclers and show distances from the device.

### Where to configure your key:
Open `app/src/main/AndroidManifest.xml` and locate lines 27–29:

```xml
<!-- Google Maps API Key -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### How to obtain and apply a Google Maps API Key:
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create or select an existing project.
3. Navigate to **APIs & Services > Library** and enable:
   - **Maps SDK for Android**
4. Go to **APIs & Services > Credentials** and click **Create Credentials > API Key**.
5. (Recommended for security) Restrict the key to Android apps using your SHA-1 fingerprint and package name `com.example.warbage`.
6. Replace `YOUR_API_KEY_HERE` in `AndroidManifest.xml` with your generated key.

> **Note:** The app runs smoothly without the key; the interactive map UI gracefully displays markers and recycler details once the key is provided.

---

## 📂 Project Architecture

```
app/src/main/java/com/example/warbage/
├── WarbageApplication.kt         # Application class (DB, WorkManager, Notification channel)
├── MainActivity.kt               # Single Activity host with Jetpack Navigation
├── ai/
│   └── MaterialClassifier.kt    # Camera image heuristic classifier for e-waste detection
├── data/
│   ├── WarbageDatabase.kt       # Room Database with prepopulated materials & recyclers
│   ├── dao/
│   │   ├── MaterialDao.kt       # Material category CRUD & queries
│   │   ├── RecyclerDao.kt       # Recycler directory queries
│   │   ├── LotDao.kt            # E-Waste lot queries & insertions
│   │   └── RequestDao.kt        # Pickup/Handover request state queries
│   └── entity/
│       ├── MaterialEntity.kt   # Material category entity & initial dataset
│       ├── RecyclerEntity.kt   # Authorized recycler entity & dataset
│       ├── LotEntity.kt        # Created scrap lot entity
│       └── RequestEntity.kt    # Transaction request entity
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt             # Main navigation dashboard
│   │   ├── MaterialListScreen.kt     # Browse e-waste categories & rates
│   │   ├── MaterialDetailScreen.kt   # Weight calculator & estimated value
│   │   ├── CreateLotScreen.kt        # Confirm and store lot
│   │   ├── RecyclerListScreen.kt     # Filterable list of certified recyclers
│   │   ├── RecyclerMapScreen.kt      # Google Maps + location distance view
│   │   ├── CreateRequestScreen.kt    # Link lot with recycler to submit request
│   │   ├── TransactionListScreen.kt  # User transaction history & statuses
│   │   ├── RecyclerDashboardScreen.kt# Recycler incoming requests overview
│   │   ├── RequestDetailScreen.kt    # Accept/Reject/Complete workflow actions
│   │   └── AiScannerScreen.kt        # CameraX viewfinder with real-time classification
│   └── theme/
│       ├── Color.kt             # Emerald green eco-friendly palette
│       ├── Theme.kt             # Material 3 dynamic color scheme
│       └── Type.kt              # Modern typography tokens
├── util/
│   └── NotificationHelper.kt    # Channel management and status alert notifications
└── worker/
    └── SyncWorker.kt            # WorkManager background sync worker
```

---

## 🛡️ Security & Clean Code Practices
- **No Sensitive Secrets**: No private API keys, cloud passwords, or personal credentials are hardcoded or tracked in git.
- **Git Hygiene**: Comprehensive `.gitignore` configured to omit `.gradle`, `build/`, `local.properties`, IDE metadata (`.idea/`), and compiled APKs.
- **Offline Capability**: The core app operates without external network dependencies thanks to Room local database seeding.

---

## 👨‍💻 Maintainer & Contact

- **Author**: Manish Raj Kamal
- **Email**: [manish.12324128@lpu.in](mailto:manish.12324128@lpu.in)
- **Repository**: [https://github.com/manish-raj-kamal/Warbage](https://github.com/manish-raj-kamal/Warbage)
