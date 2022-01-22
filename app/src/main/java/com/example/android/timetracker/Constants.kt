package com.example.android.timetracker

object Constants {

    const val DAY_DATABASE_NAME = "day_db"
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TIMER_FRAGMENT = "ACTION_SHOW_TIMER_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "timer_channel"
    const val NOTIFICATION_CHANNEL_NAME ="Timer"
    const val NOTIFICATION_ID = 1

    const val TIMER_UPDATE_INTERVAL = 50L

    const val TOTAL_MILLIS_IN_ONE_DAY = 86400000L
    const val MILLIS_IN_5_MINUTES = 300000L
    const val MILLIS_IN_1_MINUTE = 60000L
    const val TESTING_MILLIS = 86359000L

    const val EMPTY_NOTE = "Empty note..."

}