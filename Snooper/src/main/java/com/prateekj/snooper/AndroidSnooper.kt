package com.prateekj.snooper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.prateekj.snooper.infra.AppLifecycleObserver
import com.prateekj.snooper.infra.CurrentActivityManager
import com.prateekj.snooper.networksnooper.activity.HttpCallListActivity
import com.prateekj.snooper.networksnooper.database.SnooperRepo
import com.prateekj.snooper.networksnooper.model.HttpCall
import com.prateekj.snooper.networksnooper.model.HttpCallRecord
import java.io.IOException
import java.lang.ref.WeakReference

class AndroidSnooper private constructor() : CurrentActivityManager.Listener, SnooperShakeAction {
  private lateinit var context: Context
  private lateinit var snooperRepo: SnooperRepo
  private lateinit var writeThread: HandlerThread
  private lateinit var writeHandler: Handler
  private var currentActivityRef: WeakReference<Activity>? = null

  @Throws(IOException::class)
  fun record(httpCall: HttpCall) {
    writeHandler.post { this@AndroidSnooper.snooperRepo.save(HttpCallRecord.from(httpCall)) }
  }

  private fun launchActivityOnShake(application: Application) {
    val shakeDetector = ShakeDetector(SnooperShakeListener(this))

    val appLifecycleObserver = AppLifecycleObserver(context, shakeDetector)
    ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

    CurrentActivityManager.getInstance(application).registerListener(this)
  }

  override fun currentActivity(activity: Activity) {
    this.currentActivityRef = WeakReference(activity)
  }

  override fun startSnooperFlow() {
    currentActivityRef?.get()?.apply {
      val intent = Intent(this, HttpCallListActivity::class.java)
      startActivity(intent)
    }
  }

  override fun endSnooperFlow() {
    val intent = Intent(ACTION_END_SNOOPER_FLOW)
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
  }

  companion object {
    const val ACTION_END_SNOOPER_FLOW = "com.snooper.END_SNOOPER_FLOW"

    @Volatile
    private var INSTANCE: AndroidSnooper? = null

    @JvmStatic
    fun init(application: Application, launchActivityOnShake: Boolean = false) {
      INSTANCE ?: synchronized(this) {
        INSTANCE ?: buildAndroidSnooper(application, launchActivityOnShake)
          .apply { INSTANCE = this }
      }
    }

    private fun buildAndroidSnooper(
      application: Application,
      launchActivityOnShake: Boolean,
    ): AndroidSnooper {
      val androidSnooper = AndroidSnooper()
      androidSnooper.context = application
      androidSnooper.snooperRepo = SnooperRepo(androidSnooper.context)
      androidSnooper.writeThread = HandlerThread("AndroidSnooper:Writer")
      androidSnooper.writeThread.start()
      androidSnooper.writeHandler = Handler(androidSnooper.writeThread.looper)

      if (launchActivityOnShake) {
        androidSnooper.launchActivityOnShake(application)
      }

      return androidSnooper
    }

    @JvmStatic
    val instance: AndroidSnooper
      get() {
        if (INSTANCE == null) {
          throw RuntimeException("Android Snooper is not initialized yet")
        }
        return INSTANCE!!
      }
  }
}
