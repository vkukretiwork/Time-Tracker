package com.example.android.timetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.android.timetracker.Constants.ACTION_PAUSE_SERVICE
import com.example.android.timetracker.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.android.timetracker.Constants.ACTION_STOP_SERVICE
import com.example.android.timetracker.Constants.NOTIFICATION_CHANNEL_ID
import com.example.android.timetracker.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.android.timetracker.Constants.NOTIFICATION_ID
import com.example.android.timetracker.Constants.TIMER_UPDATE_INTERVAL
import com.example.android.timetracker.Constants.TOTAL_MILLIS_IN_ONE_DAY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class TimerService : LifecycleService() {

//    SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date())
    @Inject
    lateinit var repo : MainRepository
    var isFirstRunOfService = true
    var serviceKilled = false
    @Inject
    lateinit var baseNotificationBuilder : NotificationCompat.Builder
    lateinit var curNotificationBuilder : NotificationCompat.Builder
    private lateinit var currentDate : String

    companion object{
        val isTimerRunning = MutableLiveData<Boolean>()
        val timeRunInMillisTillNow = MutableLiveData<Long>()
        val timeRunInSecondsTillNow = MutableLiveData<Long>()
        var serviceRunning = MutableLiveData<Boolean>()
    }

    private fun postInitialValues(){
        isTimerRunning.postValue(false)
        currentDate = SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date())
    }

    override fun onCreate() {
        super.onCreate()

        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()

        isTimerRunning.observe(this, Observer {
//            updateTimerNotificationState(it)
        })
    }

    private fun pauseService(){
        isTimerRunning.postValue(false)
    }
    private fun killService(){
        serviceKilled = true
        isFirstRunOfService = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRunOfService){
                        startForegroundService()
                        isFirstRunOfService = false
                    }else {
                        startTimer()
                    }
                    serviceRunning.postValue(true)
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    serviceRunning.postValue(true)
                }
                ACTION_STOP_SERVICE -> {
                    serviceRunning.postValue(false)
                    killService()

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        startTimer()
        isTimerRunning.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSecondsTillNow.observe(this, Observer {
            if(!serviceKilled){
                val notification = curNotificationBuilder
                        .setContentText(TimerUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
            val instDate = SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date())
            if(instDate != currentDate){
                repo.getDayWithDate(currentDate)
                        .observe(this, Observer{ day ->
                            if(day != null){
                                day.studyDurationOfDayInMillis = timeRunInMillisTillNow.value!!
                                CoroutineScope(Dispatchers.Main).launch {
                                    repo.updateDay(day)
                                }
                            }
                        })
                pauseService()
                serviceRunning.postValue(false)
                killService()
            }
            if(it*1000L >= TOTAL_MILLIS_IN_ONE_DAY){
                pauseService()
                killService()
                timeRunInMillisTillNow.value = TOTAL_MILLIS_IN_ONE_DAY
                Toast.makeText(applicationContext,"24 hour limit reached",Toast.LENGTH_SHORT).show()
            }
        })
    }

//    private var previousTimeRunInMillisValue = 0L
    private fun startTimer(){
        isTimerRunning.postValue(true)
        var lapTime: Long
        val timeRunInMillisValueWhenTimerStarted = timeRunInMillisTillNow.value!!
//        if(isFirstRunOfService){ previousTimeRunInMillisValue = timeRunInMillisTillNow.value!! }

        timeRunInSecondsTillNow.value = timeRunInMillisTillNow.value!! / 1000L

        val timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while(isTimerRunning.value!!){

                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillisTillNow.postValue(timeRunInMillisValueWhenTimerStarted + lapTime)

//                if(timeRunInMillisTillNow.value!! >= previousTimeRunInMillisValue + 1000L){
//                    timeRunInSecondsTillNow.postValue(timeRunInSecondsTillNow.value!! + 1)
//                    previousTimeRunInMillisValue += 1000L
//                }

                val x = timeRunInMillisTillNow.value!! / 1000L
                if(x> timeRunInSecondsTillNow.value!!) timeRunInSecondsTillNow.value = x

                delay(TIMER_UPDATE_INTERVAL)
            }
        }
    }

    private fun updateTimerNotificationState(isTimerRunning: Boolean) {
        val notificationActionText = if(isTimerRunning) "Pause" else "Resume"
        val pendingIntent = if(isTimerRunning) {
            val pauseIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val stopIntent = Intent(this,TimerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val pendingIntentForStop = PendingIntent.getService(this,3,stopIntent, FLAG_UPDATE_CURRENT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled){
            curNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.ic_baseline_pause_24, notificationActionText, pendingIntent)
            if(!isTimerRunning)
                curNotificationBuilder.addAction(R.drawable.ic_baseline_stop_24,"Stop",pendingIntentForStop)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

//    *****NOT THAT IMPORTANT*****

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager : NotificationManager) {
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

}















