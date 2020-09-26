package com.prateekj.snooper.infra

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.prateekj.snooper.ShakeDetector

class AppLifecycleObserver(private val context: Context, private val shakeDetector: ShakeDetector) : LifecycleObserver {

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onEnterForeground() {
    registerSensorListener()
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onEnterBackground() {
    unregisterSensorListener()
  }

  private fun registerSensorListener() {
    val sManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    sManager.registerListener(shakeDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL)
  }

  private fun unregisterSensorListener() {
    val sManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    sManager.unregisterListener(shakeDetector)
  }
}