package com.example.android.timetracker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings){

    private val viewModel : MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "Settings"
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
        subscribeToButtons()
    }

    private fun subscribeToButtons(){
        tvResetTimerSetting.setOnClickListener {
            showResetTimerDialog()
        }
        tvContactUs.setOnClickListener {
            composeContactUsEmail(arrayOf("vkukretiwork@gmail.com"), "Regarding Time Tracker App")
        }
        tvPrivacyPolicy.setOnClickListener {
            PrivacyPolicyFragment().show(parentFragmentManager, "PrivacyPolicyBottomSheetDialog")
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
    private fun composeContactUsEmail(addresses: Array<String>, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        startActivity(intent)
    }

    private fun getCurrDate() = SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date())
}
