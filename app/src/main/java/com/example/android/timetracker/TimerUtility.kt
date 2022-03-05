package com.example.android.timetracker

import androidx.fragment.app.viewModels
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

    fun getMonthYY(yyMM : String): String {
        val yy = yyMM.substring(0,2)
        val mm = yyMM.substring(3,5)
        val month = when(mm){
            "01" -> "January"
            "02" -> "February"
            "03" -> "March"
            "04" -> "April"
            "05" -> "May"
            "06" -> "June"
            "07" -> "July"
            "08" -> "August"
            "09" -> "September"
            "10" -> "October"
            "11" -> "November"
            else -> "December"
        }
        return "$month, $yy"
    }

}