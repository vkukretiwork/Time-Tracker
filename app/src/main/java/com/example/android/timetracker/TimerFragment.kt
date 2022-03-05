package com.example.android.timetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android.timetracker.Constants.ACTION_PAUSE_SERVICE
import com.example.android.timetracker.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.android.timetracker.Constants.ACTION_STOP_SERVICE
import com.example.android.timetracker.Constants.MILLIS_IN_1_MINUTE
import com.example.android.timetracker.Constants.MILLIS_IN_5_MINUTES
import com.example.android.timetracker.Constants.TOTAL_MILLIS_IN_ONE_DAY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_timer.*
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TimerFragment : Fragment(R.layout.fragment_timer) {

    private val viewModel : MainViewModel by viewModels()
    private var isTimerRunningTF = false
    private var timeRunInMillisTillNowTF = 0L
    private lateinit var currentDate : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "Time Tracker"
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.insertDay(Day(getCurrDate(),0L))

        subscribeToButtons()
        subscribeToObservers()
        currentDate = TimerUtility.getCurrDateYYmmDD()
    }

    private fun subscribeToButtons(){
        btnStartPauseTimer.setOnClickListener{
            startPauseTimer()
        }
        btnStopTimerService.setOnClickListener{
            sendCommandToService(ACTION_STOP_SERVICE)
        }
        tvPlus5M.setOnClickListener {
            addMinutes(MILLIS_IN_5_MINUTES)
        }
        tvMinus5M.setOnClickListener {
            subtractMinutes(MILLIS_IN_5_MINUTES)
        }
        tvPlus1M.setOnClickListener {
            addMinutes(MILLIS_IN_1_MINUTE)
        }
        tvMinus1M.setOnClickListener {
            subtractMinutes(MILLIS_IN_1_MINUTE)
        }
        tvNoteOfTheDayInfoTF.setOnClickListener{
            showAddNoteDialog()
        }
        tvNoteOfTheDay.movementMethod = ScrollingMovementMethod()
    }
    private fun subscribeToObservers() {
        TimerService.isTimerRunning.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateTimer(it)
        })
        TimerService.timeRunInMillisTillNow.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            timeRunInMillisTillNowTF = it
            val formattedTime = TimerUtility.getFormattedStopWatchTime(timeRunInMillisTillNowTF, true)
            tvTimer.text = formattedTime
        })
        viewModel.currDay.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it == null)viewModel.insertDay(Day(getCurrDate(),0L))
            else if(!isTimerRunningTF){
                TimerService.timeRunInMillisTillNow.value = it.studyDurationOfDayInMillis
                TimerService.timeRunInSecondsTillNow.value =
                        (TimerService.timeRunInMillisTillNow.value
                                ?.minus(TimerService.timeRunInMillisTillNow.value!! % 1000L))
                                ?.div(1000L)
            }
            if(it != null){
                tvNoteOfTheDay.text = it.noteOfTheDay
            }
        })
        TimerService.serviceRunning.observe(viewLifecycleOwner,{
            if(it){
                enableTimerAdjustingButtons(false)
            }else{
                enableTimerAdjustingButtons(true)
            }
        })
    }

    private fun sendCommandToService(action : String) =
            Intent(requireContext(),TimerService::class.java).also {
                it.action = action
                requireContext().startService(it)
            }

    private fun updateTimer(isTimerRunning : Boolean){
        this.isTimerRunningTF = isTimerRunning
        if(!isTimerRunning){
            btnStartPauseTimer.text = "Start"
            btnStopTimerService.isEnabled = true
        } else {
            btnStartPauseTimer.text = "Pause"
            btnStopTimerService.isEnabled = false
            enableTimerAdjustingButtons(false)
        }
    }
    private fun startPauseTimer(){
        if(isTimerRunningTF){
            val day = viewModel.currDay.value!!
            day.studyDurationOfDayInMillis = timeRunInMillisTillNowTF
            viewModel.updateDay(day)
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else {
                sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun getCurrDate() = SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date())

    private fun enableTimerAdjustingButtons(providedValue : Boolean){
        tvPlus1M.isEnabled = providedValue
        tvPlus5M.isEnabled = providedValue
        tvMinus1M.isEnabled = providedValue
        tvMinus5M.isEnabled = providedValue
    }
    private fun addMinutes(providedMinutes : Long) {
        val valueAfterAdding5Min = TimerService.timeRunInMillisTillNow.value?.plus(providedMinutes)
        if (valueAfterAdding5Min != null) {
            if(valueAfterAdding5Min >= TOTAL_MILLIS_IN_ONE_DAY){
                TimerService.timeRunInMillisTillNow.value = TOTAL_MILLIS_IN_ONE_DAY
            } else {
                TimerService.timeRunInMillisTillNow.value = valueAfterAdding5Min
            }
            val day = viewModel.currDay.value!!
            day.studyDurationOfDayInMillis = timeRunInMillisTillNowTF
            viewModel.updateDay(day)
            val instDate = TimerUtility.getCurrDateYYmmDD()
            if(instDate != currentDate)navigateBackAndForth()
        }
    }
    private fun navigateBackAndForth(){
        navHostFragment.findNavController().navigate(R.id.daysListFragment)
        navHostFragment.findNavController().navigate(R.id.timerFragment)
    }

    private fun subtractMinutes(providedMinutes : Long) {
        val valueAfterSubtracting5Min = TimerService.timeRunInMillisTillNow.value?.minus(providedMinutes)
        if (valueAfterSubtracting5Min != null) {
            if(valueAfterSubtracting5Min <= 0L){
                TimerService.timeRunInMillisTillNow.value = 0L
            } else {
                TimerService.timeRunInMillisTillNow.value = valueAfterSubtracting5Min
            }
            val day = viewModel.currDay.value!!
            day.studyDurationOfDayInMillis = timeRunInMillisTillNowTF
            viewModel.updateDay(day)
            val instDate = TimerUtility.getCurrDateYYmmDD()
            if(instDate != currentDate)navigateBackAndForth()
        }
    }

    private fun showAddNoteDialog(){
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_note,null)
        val editText = dialogLayout.findViewById<EditText>(R.id.etAddNote)
        val day = viewModel.currDay.value!!
        editText.setText(day.noteOfTheDay)

        with(builder){
            setTitle("Add note")
            setPositiveButton("ok"){ dialog, which ->
                val text = editText.text.toString()
                day.noteOfTheDay = text
                viewModel.updateDay(day)
            }
            setNegativeButton("cancel"){ dialog, which ->
                dialog.cancel()
            }
            setView(dialogLayout)
        }

        val dialog = builder.create()
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
        editText.requestFocus()
    }

}