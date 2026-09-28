# EdgeGuard - On-Device AI Fire Detection & IoT Safety System

An end-to-end Physical AI safety solution combining Android On-Device Computer Vision, MQTT event dispatching, and a Web Monitoring Dashboard.

## 🏗️ System Architecture Pipeline
1. **Capture:** Android CameraX captures live video frames (30+ FPS).
2. **Inference:** Quantized YOLOv8 model runs locally via TensorFlow Lite.
3. **Dispatch:** MQTT Broker (Mosquitto) transmits alert payloads in <100ms.
4. **Monitoring:** Real-time Web Dashboard (PHP, WebSockets) receives alerts.
5. **Actuation:** Triggers IoT warning devices or robot navigation signals.

## 📁 Repository Structure
- `/android`: Android Kotlin source code for CameraX & TFLite inference.
- `/python_poc`: Python script for local desktop testing and MQTT publishing.
- `/web_dashboard`: Real-time frontend dashboard using HTML5 and MQTT WebSockets.
- `/backend`: PHP REST API and MySQL database schema for telemetry logging.

## 🛠️ Tech Stack
- **Languages:** Kotlin, Python, PHP, JavaScript, SQL
- **AI/Vision:** TensorFlow Lite, OpenCV, YOLOv8
- **IoT & Protocols:** MQTT (Mosquitto), WebSockets