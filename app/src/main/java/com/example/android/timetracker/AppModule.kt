package com.example.android.timetracker

import android.content.Context
import android.provider.SyncStateContract
import androidx.room.Room
import com.example.android.timetracker.Constants.DAY_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDayDatabase(
        @ApplicationContext app : Context
    ) = Room.databaseBuilder(
        app,
        DayDatabase::class.java,
        DAY_DATABASE_NAME
    ).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideDayDao(db : DayDatabase) = db.getDayDao()
}