package com.example.android.timetracker

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.android.timetracker.Constants.ACTION_SHOW_TIMER_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setChartYearSpinnerList()
        navigateToTimerFragmentIfNeeded(intent)

        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnItemReselectedListener { /* NO-OP */ }

        navHostFragment.findNavController()
                .addOnDestinationChangedListener{ _, destination, _ ->
                    when(destination.id){
                        R.id.timerFragment, R.id.daysListFragment, R.id.settingsFragment, R.id.chartFragment
                            -> {
                            bottomNavigationView.visibility = View.VISIBLE
                        }
                        else -> {
                            bottomNavigationView.visibility = View.GONE
                        }
                    }
                }
    }

    companion object{
        val chartYearSpinnerList = mutableListOf<String>()
    }
    private fun setChartYearSpinnerList() {
        chartYearSpinnerList.clear()
        val currDate = TimerUtility.getCurrDateYYmmDD()
        val currYear = currDate.substring(0,2)
        if(!chartYearSpinnerList.contains("20$currYear")){chartYearSpinnerList.add("20$currYear")}
        val prevYear = (currYear.toInt() - 1).toString()
        viewModel.getDaysOfYear(prevYear).observe(this , { prevYearList ->
            if(prevYearList.isNotEmpty()){
                if(!chartYearSpinnerList.contains("20$prevYear")){chartYearSpinnerList.add("20$prevYear")}
                val prevPrevYear = (prevYear.toInt() - 1).toString()
                viewModel.getDaysOfYear(prevPrevYear).observe(this, { prevPrevYearList ->
                    if(prevPrevYearList.isNotEmpty() && !chartYearSpinnerList.contains("20$prevPrevYear"))
                    {chartYearSpinnerList.add("20$prevPrevYear")}
                })
            }
        })
    }


//    *****NOT THAT IMPORTANT*****

//    to open app when clicked on notification
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTimerFragmentIfNeeded(intent)
    }
//    to open app when clicked on notification
    private fun navigateToTimerFragmentIfNeeded(intent : Intent?){
        if(intent?.action == ACTION_SHOW_TIMER_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_timerFragment)
        }
    }

}