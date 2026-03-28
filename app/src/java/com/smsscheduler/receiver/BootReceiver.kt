package com.smsscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smsscheduler.data.SmsDatabase
import com.smsscheduler.scheduler.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SmsDatabase.getDatabase(context)
                val activeSchedules = db.smsDao().getActiveSchedules()
                activeSchedules.forEach { schedule ->
                    AlarmScheduler.scheduleAlarm(context, schedule)
                }
                Log.d("BootReceiver", "Rescheduled ${activeSchedules.size} alarms after boot")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error rescheduling alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
