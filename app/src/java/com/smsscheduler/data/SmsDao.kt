package com.smsscheduler.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {

    @Query("SELECT * FROM sms_schedules ORDER BY hour, minute")
    fun getAllSchedules(): Flow<List<SmsSchedule>>

    @Query("SELECT * FROM sms_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Int): SmsSchedule?

    @Query("SELECT * FROM sms_schedules WHERE isActive = 1")
    suspend fun getActiveSchedules(): List<SmsSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: SmsSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: SmsSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: SmsSchedule)
}
