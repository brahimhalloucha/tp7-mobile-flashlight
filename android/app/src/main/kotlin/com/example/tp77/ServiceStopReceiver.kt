package com.example.tp77

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ServiceStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == MainActivity.ACTION_STOP_SERVICE) {
            context?.let {
                val stopIntent = Intent(it, MainActivity::class.java).apply {
                    action = MainActivity.ACTION_STOP_SERVICE
                }
                stopIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                it.startActivity(stopIntent)
            }
        }
    }
}
