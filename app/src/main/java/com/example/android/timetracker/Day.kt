package com.example.android.timetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity(tableName = "day_table")
data class Day(
    @PrimaryKey(autoGenerate = false)
    var dateOfDayYYMMDD : String,
    var studyDurationOfDayInMillis : Long = 0L
) {
    var noteOfTheDay = ""
}