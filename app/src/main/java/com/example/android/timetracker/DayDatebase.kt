package com.example.android.timetracker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Day::class],
    version = 18
)
abstract class DayDatabase : RoomDatabase() {

    abstract fun getDayDao() : DayDao
}