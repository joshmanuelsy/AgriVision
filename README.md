# AgriVision 🌿

AgriVision is an advanced, AI-powered crop health monitoring application designed to help farmers detect and manage plant diseases efficiently. By combining edge-based machine learning with state-of-the-art cloud AI, AgriVision provides real-time diagnostic reports from both standard mobile devices and E99/HF UFO-style drones.

## 🚀 Key Features

-   **Dual Scanning Modes**:
    -   **Tablet Camera**: High-resolution, local crop analysis using CameraX.
    -   **Drone Integration**: Live UDP video streaming from E99/HF UFO drones for large-scale aerial field monitoring.
-   **Intelligent Analysis**:
    -   **Local TFLite Classifier**: Instant, offline disease detection using a custom-trained TensorFlow Lite model.
    -   **AI Agronomist (Gemini 3.6 Flash)**: Detailed multimodal diagnostic reports providing organic remedies and preventative actions.
-   **Smart Workflow**:
    -   **Capture & Reconnect**: Specifically optimized for drone usage. Capture a frame, switch to an internet-connected network, and get your AI report immediately.
-   **Comprehensive History**: Track crop health over time with a detailed dashboard and historical scan database.
-   **Modern UI**: Built entirely with Jetpack Compose using a custom Material 3 "AgriVision" theme.

## 🛠 Tech Stack

-   **Language**: Kotlin
-   **UI Framework**: Jetpack Compose
-   **AI/ML**: Google Gemini (Generative AI SDK), TensorFlow Lite
-   **Camera**: CameraX
-   **Networking**: Kotlin Coroutines & Sockets (TCP/UDP) for Drone protocol integration

## 📋 Setup Instructions

### Prerequisites
-   Android Studio Ladybug or newer
-   JDK 17+
-   A Google Gemini API Key (obtainable from [Google AI Studio](https://aistudio.google.com/))

### Configuration
1.  Clone the repository:
    ```bash
    git clone https://github.com/joshmanuelsy/AgriVision.git
    ```
2.  Open the project in Android Studio.
3.  Create a `local.properties` file in the root directory (if not already present).
4.  Add your Gemini API key:
    ```properties
    GEMINI_API_KEY=YOUR_API_KEY_HERE
    ```

### Drone Connection
To use the drone feature, connect your device to the drone's Wi-Fi access point (e.g., `HF_266f8b4f`) before initiating the scan.

## 🤝 Contributing
Contributions are welcome! Feel free to open issues or submit pull requests to improve the diagnostic accuracy or add support for more drone protocols.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
