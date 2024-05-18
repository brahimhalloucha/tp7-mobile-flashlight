package com.example.tp77

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.core.app.NotificationCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import androidx.annotation.NonNull

class MainActivity : FlutterActivity() {
    private var flashLightStatus = false

    // Variables for proximity sensor and sensor manager
    private var proximitySensor: Sensor? = null
    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize sensor manager and proximity sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        // Check if the intent contains the stop action
        if (intent?.action == ACTION_STOP_SERVICE) {
            StopService()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ACTION_STOP_SERVICE) {
            StopService()
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "messages"
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "StartService" -> {
                    StartService()
                    result.success("OK")
                }
                "StopService" -> {
                    StopService()
                    result.success("OK")
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun createNotificationChannel() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Notification for Proximity Sensor Service"
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun StartService() {
        createNotificationChannel() // Call only if targeting Oreo or above
        val stopServiceIntent = Intent(this, ServiceStopReceiver::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Proximity Sensor Service")
            .setContentText("Service is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false) // Prevent automatic cancellation on tap
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use a built-in Android icon
            .addAction(android.R.drawable.ic_delete, "Stop Service", pendingIntent) // Add stop action

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
        sensorManager!!.registerListener(
            proximitySensorEventListener,
            proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun StopService() {
        // Unregister proximity sensor listener
        sensorManager!!.unregisterListener(proximitySensorEventListener)
        // Turn off flashlight when stopping service
        turnOffFlashlight()

        // Remove notification when service stops
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

    private fun turnOnFlashlight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId: String
        try {
            cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
            flashLightStatus = true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOffFlashlight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId: String
        try {
            cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
            flashLightStatus = false
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val proximitySensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not needed for this implementation
        }

        override fun onSensorChanged(event: SensorEvent) {
            // Check if the sensor type is proximity sensor
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                // Check proximity value
                if (event.values[0] < proximitySensor!!.maximumRange) {
                    // Close to the sensor
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

    companion object {
        const val CHANNEL_ID = "proximity_sensor_service" // Notification Channel ID
        const val CHANNEL_NAME = "Proximity Sensor Service" // Notification Channel Name
        const val ACTION_STOP_SERVICE = "com.example.tp77.action.STOP_SERVICE" // Action for stopping service
    }
}
