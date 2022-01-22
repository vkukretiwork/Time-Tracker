package com.example.android.timetracker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
            @ApplicationContext app : Context
    ) = PendingIntent.getActivity(
            app,
            0,
            Intent(app, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TIMER_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
            @ApplicationContext app : Context,
            pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_timer_24)
            .setContentTitle("Time Tracker")
            .setContentText("00:00:00")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
}