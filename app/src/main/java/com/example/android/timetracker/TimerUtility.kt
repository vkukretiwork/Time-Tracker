package com.example.android.timetracker

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimerUtility {

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false) : String{
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if(!includeMillis){
            return "${if(hours < 10) "0" else ""}$hours:"+
                    "${if(minutes < 10) "0" else ""}$minutes:"+
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:"+
                "${if(minutes < 10) "0" else ""}$minutes:"+
                "${if(seconds < 10) "0" else ""}$seconds:"+
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }
    fun reverseDate(date : String): String{
        val list = date.split(".")
        return list[2]+"."+list[1]+"."+list[0]
    }
    fun getCurrDateYYmmDD(): String = SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date())
}