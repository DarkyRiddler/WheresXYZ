# Where's XYZ 📍 - Real-time Location & Group Management

**Where's XYZ** is a comprehensive Android application built for users who value staying connected. Whether you're traveling in a group, meeting up with friends in a crowded city, or coordinating a family outing, this app provides the tools to ensure no one gets lost.

---

## 🚀 Key Features in Detail

### 🔐 Advanced Security & Auth
*   **Firebase Authentication**: Robust email and password-based registration and login system.
*   **Biometric Integration**: Quick and secure access using Fingerprint or Face Recognition via Android BiometricPrompt API.
*   **Encrypted Storage**: Sensitive user data is handled using `EncryptedSharedPreferences` for local persistence.

### 👥 Dynamic Group Coordination
*   **Unique Group Codes**: Groups are identified by easy-to-share 4-digit codes, simplifying the joining process.
*   **Many-to-Many Relationships**: A flexible system where users can be members of multiple circles (e.g., "Family", "Work Friends", "Travel Squad") simultaneously.
*   **Real-time Synchronization**: Powered by Firestore, every group change (new member, name change) is reflected instantly across all devices.

### 🗺️ Live Mapping & Interaction
*   **Google Maps Integration**: High-precision location tracking using the Google Maps SDK for Android.
*   **Interactive Pings (1:N)**: A unique communication system allowing users to send pre-defined or custom emoji-based pings to catch attention without typing.
*   **Event Management (1:1)**: Every group can host a specific event with dedicated descriptions, photos, and timeframes, visible to all members on the map.

---

## 🛠 Technical Architecture

The project follows the **Clean Architecture** principles and **MVVM (Model-View-ViewModel)** design pattern to ensure scalability and testability.

*   **Jetpack Compose**: 100% declarative UI built with modern Material 3 components.
*   **Hilt (Dagger)**: Standardized dependency injection for modular code.
*   **Kotlin Coroutines & Flow**: Asynchronous programming for smooth, non-blocking UI and real-time data streams.
*   **Firebase Suite**:
    *   **Cloud Firestore**: Real-time NoSQL database.
    *   **Firebase Auth**: User management.
    *   **Cloud Storage**: For storing user avatars and group photos.
*   **Retrofit & Gson**: Ready-to-use networking stack for future REST API integrations.

---

## 📊 Database Schema (Firestore)

| Collection | Description | Relationships |
| :--- | :--- | :--- |
| `users` | User profiles, settings, and group lists. | `groups` (Many-to-Many) |
| `groups` | Group metadata, codes, and member UIDs. | `users` (M:M), `events` (1:1) |
| `events` | Details for group-specific gatherings. | `groups` (1:1) |
| `pings` | Interactive alerts sent by users. | `users` (1:N) |

---

## ⚙️ Setup and Installation

### Prerequisites
*   Android Studio Ladybug or newer.
*   JDK 17+.
*   A Firebase Project.

### Steps
1.  **Clone the Repo**: `git clone https://github.com/your-username/wheresxyz.git`
2.  **Firebase Config**: 
    *   Go to Firebase Console, create an Android app with package `com.example.wheresxyz`.
    *   Download `google-services.json` and place it in the `app/` directory.
3.  **API Keys**:
    *   Open `local.properties` in the root folder.
    *   Add: `MAPS_API_KEY=your_google_maps_key_here`.
4.  **Build**: Sync Gradle and click "Run".

---

## 🛡️ License
This project was developed for educational purposes as part of a Semester 6 project.
