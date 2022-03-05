package com.example.android.timetracker

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface DayDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDay(day : Day)

    @Update
    suspend fun updateDay(day : Day)

    @Delete
    suspend fun deleteDay(day : Day)

    @Query("Delete From day_table where dateOfDayYYMMDD = :dateProvided")
    suspend fun deleteDayWithDate(dateProvided : String)

    @Query("Delete From day_table")
    suspend fun deleteAllDatabase()

    @Query("SELECT * FROM day_table ORDER BY dateOfDayYYMMDD DESC")
    fun getAllDaysSortedByDate() : LiveData<List<Day>>

    @Query("Select * From day_table Order By studyDurationOfDayInMillis DESC")
    fun getAllDaysSortedByTime() : LiveData<List<Day>>

    @Query("Select * from day_table where dateOfDayYYMMDD = :dateProvided")
    fun getDayWithDate(dateProvided : String) : LiveData<Day>

//    @Query("Select SUM(studyDurationOfDayInMillis) From day_table")
//    fun getTotalStudyTimeInMillis() : LiveData<Long>

    @Query("select * from day_table where dateOfDayYYMMDD like :month || '%' order by dateOfDayYYMMDD asc")
    fun getDaysOfMonthYYmmSortedByDateASC(month : String) : LiveData<List<Day>>

    @Query("select * from day_table where dateOfDayYYMMDD like :month || '%' order by dateOfDayYYMMDD DESC")
    fun getDaysOfMonthYYmmSortedByDateDESC(month : String) : LiveData<List<Day>>

    @Query("select * from day_table where dateOfDayYYMMDD like :month || '%' order by studyDurationOfDayInMillis DESC")
    fun getDaysOfMonthYYmmSortedByTime(month : String) : LiveData<List<Day>>

    @Query("select * from day_table where dateOfDayYYMMDD like :year || '%'")
    fun getDaysOfYear(year : String) : LiveData<List<Day>>


}