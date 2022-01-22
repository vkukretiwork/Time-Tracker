package com.example.android.timetracker

import javax.inject.Inject

class MainRepository @Inject constructor(
        val dayDao: DayDao
) {

    suspend fun insertDay(day : Day) = dayDao.insertDay(day)
    suspend fun updateDay(day : Day) = dayDao.updateDay(day)
    suspend fun deleteDay(day : Day) = dayDao.deleteDay(day)
    suspend fun deleteDayWithDate(dateProvided : String) = dayDao.deleteDayWithDate(dateProvided)
    suspend fun deleteAllDatabase() = dayDao.deleteAllDatabase()
    fun getAllDaysSortedByDate() = dayDao.getAllDaysSortedByDate()
    fun getAllDaysSortedByTime() = dayDao.getAllDaysSortedByTime()
    fun getTotalStudyTimeInMillis() = dayDao.getTotalStudyTimeInMillis()
    fun getDayWithDate(dateProvided: String) = dayDao.getDayWithDate(dateProvided)

    fun getDaysOfMonthYYmm(month : String) = dayDao.getDaysOfMonthYYmm(month)
    fun getDaysOfYear(year : String) = dayDao.getDaysOfYear(year)
}