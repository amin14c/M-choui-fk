package com.smsscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import com.smsscheduler.data.SmsDatabase
import com.smsscheduler.scheduler.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("schedule_id", -1)
        if (scheduleId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SmsDatabase.getDatabase(context)
                val schedule = db.smsDao().getScheduleById(scheduleId)

                if (schedule != null && schedule.isActive) {
                    sendSms(context, schedule.phoneNumber, schedule.message)
                    Log.d("SmsAlarmReceiver", "SMS sent to ${schedule.recipientName}")
                    AlarmScheduler.scheduleAlarm(context, schedule)
                }
            } catch (e: Exception) {
                Log.e("SmsAlarmReceiver", "Error in alarm receiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val smsManager = context.getSystemService(SmsManager::class.java)
                smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                @Suppress("DEPRECATION")
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
        } catch (e: Exception) {
            Log.e("SmsAlarmReceiver", "Failed to send SMS to $phoneNumber", e)
        }
    }
}
