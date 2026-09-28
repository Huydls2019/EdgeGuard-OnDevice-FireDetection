import cv2
import json
import time
import numpy as np
import paho.mqtt.client as mqtt
import tensorflow as tf

MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC_ALERT = "edgeguard/safety/alerts"

client = mqtt.Client(client_id="Edge_Python_Node")
client.connect(MQTT_BROKER, MQTT_PORT, 60)

interpreter = tf.lite.Interpreter(model_path="yolov8n_fire_quantized.tflite")
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

cap = cv2.VideoCapture(0)

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    resized = cv2.resize(frame, (320, 320))
    input_data = np.expand_dims(resized / 255.0, axis=0).astype(np.float32)

    start_time = time.time()
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    latency_ms = (time.time() - start_time) * 1000

    max_conf = np.max(output_data[0, 4, :])
    
    if max_conf > 0.65:
        payload = {
            "event": "FIRE_DETECTED",
            "confidence": float(round(max_conf, 2)),
            "latency_ms": float(round(latency_ms, 2)),
            "timestamp": int(time.time() * 1000),
            "bbox": {"x": 160.0, "y": 160.0, "w": 50.0, "h": 50.0}
        }
        client.publish(TOPIC_ALERT, json.dumps(payload))
        cv2.putText(frame, f"FIRE DETECTED ({max_conf:.2f})", (20, 50),
                    cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)

    cv2.imshow("EdgeGuard Camera Feed", frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()