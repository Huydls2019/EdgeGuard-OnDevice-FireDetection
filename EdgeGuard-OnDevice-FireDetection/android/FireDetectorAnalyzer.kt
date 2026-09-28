package com.edgeguard.ai

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FireDetectorAnalyzer(
    context: Context,
    private val tfliteModel: ByteBuffer,
    private val mqttBrokerUrl: String = "tcp://192.168.1.100:1883"
) : ImageAnalysis.Analyzer {

    private val interpreter = Interpreter(tfliteModel)
    private var mqttClient: MqttClient? = null
    
    private val inputWidth = 320
    private val inputHeight = 320
    private val confidenceThreshold = 0.65f

    init {
        try {
            mqttClient = MqttClient(mqttBrokerUrl, "Android_EdgeGuard_Node", MemoryPersistence())
            mqttClient?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, false)
        
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val outputBuffer = Array(1) { Array(5) { FloatArray(2100) } }

        interpreter.run(inputBuffer, outputBuffer)

        var maxScore = 0f
        val bestBox = FloatArray(4)

        for (i in 0 until 2100) {
            val score = outputBuffer[0][4][i]
            if (score > maxScore) {
                maxScore = score
                bestBox[0] = outputBuffer[0][0][i]
                bestBox[1] = outputBuffer[0][1][i]
                bestBox[2] = outputBuffer[0][2][i]
                bestBox[3] = outputBuffer[0][3][i]
            }
        }

        if (maxScore > confidenceThreshold) {
            publishFireAlert(maxScore, bestBox)
        }

        imageProxy.close()
    }

    private fun publishFireAlert(confidence: Float, box: FloatArray) {
        if (mqttClient?.isConnected == true) {
            val payload = JSONObject().apply {
                put("event", "FIRE_DETECTED")
                put("confidence", confidence)
                put("timestamp", System.currentTimeMillis())
                put("bbox", JSONObject().apply {
                    put("x", box[0])
                    put("y", box[1])
                    put("w", box[2])
                    put("h", box[3])
                })
            }

            val message = MqttMessage(payload.toString().toByteArray()).apply { qos = 1 }
            mqttClient?.publish("edgeguard/safety/alerts", message)
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputWidth * inputHeight * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }
        return byteBuffer
    }
}