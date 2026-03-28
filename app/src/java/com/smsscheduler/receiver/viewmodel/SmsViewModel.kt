package com.smsscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smsscheduler.data.SmsDatabase
import com.smsscheduler.data.SmsSchedule
import com.smsscheduler.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SmsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SmsDatabase.getDatabase(application).smsDao()

    val schedules = dao.getAllSchedules().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addSchedule(schedule: SmsSchedule) {
        viewModelScope.launch {
            val generatedId = dao.insertSchedule(schedule).toInt()
            val savedSchedule = schedule.copy(id = generatedId)
            if (savedSchedule.isActive) {
                AlarmScheduler.scheduleAlarm(getApplication(), savedSchedule)
            }
        }
    }

    fun updateSchedule(schedule: SmsSchedule) {
        viewModelScope.launch {
            dao.updateSchedule(schedule)
            if (schedule.isActive) {
                AlarmScheduler.scheduleAlarm(getApplication(), schedule)
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), schedule.id)
            }
        }
    }

    fun deleteSchedule(schedule: SmsSchedule) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), schedule.id)
            dao.deleteSchedule(schedule)
        }
    }

    fun toggleActive(schedule: SmsSchedule) {
        viewModelScope.launch {
            val updated = schedule.copy(isActive = !schedule.isActive)
            dao.updateSchedule(updated)
            if (updated.isActive) {
                AlarmScheduler.scheduleAlarm(getApplication(), updated)
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), updated.id)
            }
        }
    }
}
