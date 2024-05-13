package com.example.tp77

import io.flutter.embedding.android.FlutterActivity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class MainActivity : FlutterActivity() {
    var flashLightStatus: Boolean = false

    // Variables for proximity sensor and sensor manager
    lateinit var proximitySensor: Sensor
    lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize sensor manager and proximity sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) as Sensor
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "messages").setMethodCallHandler { call, result ->
            when (call.method) {
                "StartService" -> {
                    StartService()
                    result.success("OK")
                }
                "StopService" -> {
                    StopService()
                    result.success("OK")
                }
            }
        }
    }

    private fun StartService() {
        // Register proximity sensor listener
        sensorManager.registerListener(proximitySensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun StopService() {
        // Unregister proximity sensor listener
        sensorManager.unregisterListener(proximitySensorEventListener)
        // Turn off flashlight when stopping service
        turnOffFlashlight()
    }

    private fun turnOnFlashlight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        try {
            cameraManager.setTorchMode(cameraId, true)
            flashLightStatus = true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOffFlashlight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        try {
            cameraManager.setTorchMode(cameraId, false)
            flashLightStatus = false
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    var proximitySensorEventListener: SensorEventListener? = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not needed for this implementation
        }

        override fun onSensorChanged(event: SensorEvent) {
            // Check if the sensor type is proximity sensor
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                // Check proximity value
                if (event.values[0] < proximitySensor.maximumRange) {
                    // Close to the camera
                    toggleFlashlight()
                }
            }
        }
    }

    private fun toggleFlashlight() {
        if (flashLightStatus) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }
}
