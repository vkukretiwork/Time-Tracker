package com.example.android.timetracker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings){

    private val viewModel : MainViewModel by viewModels()

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
        subscribeToButtons()
    }

    private fun subscribeToButtons(){
        tvResetTimerSetting.setOnClickListener {
            showResetTimerDialog()
        }
    }
    private fun subscribeToObservers() {
        viewModel.currDay.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it == null)viewModel.insertDay(Day(getCurrDate(),0L))
        })
    }

    private fun sendCommandToService(action : String) =
            Intent(requireContext(),TimerService::class.java).also {
                it.action = action
                requireContext().startService(it)
            }

    private fun showResetTimerDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Reset")
                .setMessage("Are you sure you want to reset timer?")
                .setIcon(R.drawable.ic_baseline_restore_24_black)
                .setPositiveButton("Yes"){ _, _ ->
                    sendCommandToService(Constants.ACTION_STOP_SERVICE)
                    val day = viewModel.currDay.value!!
                    day.studyDurationOfDayInMillis = 0L
                    viewModel.updateDay(day)
                }
                .setNegativeButton("No"){ dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .create()
        dialog.show()
    }

    private fun getCurrDate() = SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date())
}
