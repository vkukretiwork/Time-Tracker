package com.example.android.timetracker

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chart.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ChartFragment : Fragment(R.layout.fragment_chart) {

    private val viewModel : MainViewModel by viewModels()
    private lateinit var sharedPrefs : SharedPreferences
    private lateinit var editorForSpinnerPref : SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "Charts"
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = requireActivity().getSharedPreferences("spinnerPref", Context.MODE_PRIVATE)
        editorForSpinnerPref = sharedPrefs?.edit()
        setupSpinners()
        setupChartsOnTheBasisOfChartTypeSpinner()

        btnApplyInChartFragment.setOnClickListener {
            onApplyButtonClicked()
        }
    }

    private fun observeOnMonth(line : Boolean){
        viewModel.getDaysOfMonthYYmmSortedByDateASC(getYYmmStringFromSpinners() )
                .observe(viewLifecycleOwner, {
            if(line){
                it?.let {
                    val allTimeInMillis =  deployEntryListForLineChart(it)
                    val lineDataSet = LineDataSet(allTimeInMillis, "Time per day").apply {
                        valueTextColor = Color.BLACK
                        color = ContextCompat.getColor(requireContext(), R.color.forest_green)
                        setDrawValues(false)
                    }
                    lineChart.apply {
                        data = LineData(lineDataSet)
                        marker = CustomMarkerView(getYYmmStringFromSpinners(), it, requireContext(), R.layout.marker_view)
                        setVisibleXRangeMinimum(10F)
                        invalidate()
                    }
                }
            }else {
                it?.let {
                    val allTimeInMillis = deployEntryListForBarChart(it)
                    val barDataSet = BarDataSet(allTimeInMillis, "Time per day").apply {
                        valueTextColor = Color.BLACK
                        color = ContextCompat.getColor(requireContext(), R.color.forest_green)
                        setDrawValues(false)
                    }
                    barChart.apply {
                        data = BarData(barDataSet)
                        marker = CustomMarkerView(getYYmmStringFromSpinners(), it, requireContext(), R.layout.marker_view)
                        setVisibleXRangeMinimum(10F)
                        invalidate()
                    }
                }
            }
        })
    }

    private fun deployEntryListForLineChart(dayList : List<Day>): MutableList<Entry> {
        val entryList = mutableListOf<Entry>()
        val monthSize = getMonthSize()
        var j = 0
        for(i in 1..monthSize){
            if( j < dayList.size &&
                    i == dayList[j].dateOfDayYYMMDD.substring(6,8).toInt() ){
                entryList.add(Entry(i.toFloat(), getHourAndMin(dayList[j].studyDurationOfDayInMillis)))
                j++
            }else {
                entryList.add(Entry(i.toFloat(), 0F))
            }
        }
        return entryList
    }
    private fun deployEntryListForBarChart(dayList : List<Day>): MutableList<BarEntry> {
        val entryList = mutableListOf<BarEntry>()
        val monthSize = getMonthSize()
        var j = 0
        for(i in 1..monthSize){
            if( j < dayList.size &&
                    i == dayList[j].dateOfDayYYMMDD.substring(6,8).toInt() ){
                entryList.add(BarEntry(i.toFloat(), getHourAndMin(dayList[j].studyDurationOfDayInMillis)))
                j++
            }else {
                entryList.add(BarEntry(i.toFloat(), 0F))
            }
        }
        return entryList
    }
    private fun getMonthSize() : Int{
        val year = spYearFilter.selectedItem.toString().toInt()
        return  when(spMonthFilter.selectedItemPosition){
            0,2,4,6,7,9,11 -> { 31 }
            3,5,8,10 -> { 30 }
            else -> {
                if(isLeapYear(year)) 29
                else 28
            }
        }
    }

    private fun setupSpinners(){
        spYearFilter.adapter = context?.let {
            ArrayAdapter(it, R.layout.support_simple_spinner_dropdown_item, MainActivity.chartYearSpinnerList.reversed())
        }

        when(sharedPrefs.getInt("yearPref",0)){
            2 -> {
                if(spYearFilter.count > 2)
                spYearFilter.setSelection(2)
                else spYearFilter.setSelection(0)
            }
            1 -> {
                if(spYearFilter.count > 1)
                spYearFilter.setSelection(1)
                else spYearFilter.setSelection(0)
            }
            else -> spYearFilter.setSelection(0)
        }

        when(sharedPrefs.getInt("monthPref",0)){
            0 -> spMonthFilter.setSelection(0)
            1 -> spMonthFilter.setSelection(1)
            2 -> spMonthFilter.setSelection(2)
            3 -> spMonthFilter.setSelection(3)
            4 -> spMonthFilter.setSelection(4)
            5 -> spMonthFilter.setSelection(5)
            6 -> spMonthFilter.setSelection(6)
            7 -> spMonthFilter.setSelection(7)
            8 -> spMonthFilter.setSelection(8)
            9 -> spMonthFilter.setSelection(9)
            10 -> spMonthFilter.setSelection(10)
            11 -> spMonthFilter.setSelection(11)
        }
        when(sharedPrefs.getInt("timeAxisPref",0)){
            0 -> spTimeAxisMaxValueFilter.setSelection(0)
            1 -> spTimeAxisMaxValueFilter.setSelection(1)
            2 -> spTimeAxisMaxValueFilter.setSelection(2)
            3 -> spTimeAxisMaxValueFilter.setSelection(3)
            4 -> spTimeAxisMaxValueFilter.setSelection(4)
            5 -> spTimeAxisMaxValueFilter.setSelection(5)
        }
        when(sharedPrefs.getInt("gridPref",0)){
            2 -> spGridFilter.setSelection(2)
            1 -> spGridFilter.setSelection(1)
            else -> spGridFilter.setSelection(0)
        }
        when(sharedPrefs.getInt("chartTypePref", 0)){
            1 -> spChartTypeFilter.setSelection(1)
            else -> spChartTypeFilter.setSelection(0)
        }
    }

    private fun setupChartsOnTheBasisOfChartTypeSpinner(){
        if(spChartTypeFilter.selectedItemPosition == 1){
            barChart.visibility = View.VISIBLE
            lineChart.visibility = View.GONE
            setupChartLayout(false)
            observeOnMonth(false)
        } else {
            lineChart.visibility = View.VISIBLE
            barChart.visibility = View.GONE
            setupChartLayout(true)
            observeOnMonth(true)
        }
        tvMonthYYCharts.text = TimerUtility.getMonthYY(getYYmmStringFromSpinners())
    }

    private fun setupChartLayout(line : Boolean){
        val currChart = if(line)lineChart
                    else barChart

        currChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            if(spGridFilter.selectedItemPosition == 0)
                setDrawGridLines(true)
            else setDrawGridLines(false)
            labelCount = 10
        }
        currChart.axisLeft.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            if(spGridFilter.selectedItemPosition == 1)
                setDrawGridLines(false)
            else setDrawGridLines(true)
            axisMinimum = 0F
            val timeAxisMaxValue = getTimeAxisMaxValueFromSpinner()
            axisMaximum = timeAxisMaxValue.toFloat()
            labelCount = timeAxisMaxValue
        }
        currChart.apply {
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            setVisibleYRangeMinimum(getTimeAxisMaxValueFromSpinner().toFloat(), YAxis.AxisDependency.LEFT)
        }
    }

    private fun onApplyButtonClicked(){
        setupChartsOnTheBasisOfChartTypeSpinner()

        editorForSpinnerPref.apply {
            putInt("yearPref", spYearFilter.selectedItemPosition)
            putInt("monthPref", spMonthFilter.selectedItemPosition)
            putInt("timeAxisPref", spTimeAxisMaxValueFilter.selectedItemPosition)
            putInt("gridPref",spGridFilter.selectedItemPosition)
            putInt("chartTypePref", spChartTypeFilter.selectedItemPosition)
            apply()
        }
    }

    private fun getYYmmStringFromSpinners() : String {
        val year =  spYearFilter.selectedItem.toString().substring(2,4)
        val month = when(spMonthFilter.selectedItemPosition){
            0 -> {"01"}
            1 -> {"02"}
            2 -> {"03"}
            3 -> {"04"}
            4 -> {"05"}
            5 -> {"06"}
            6 -> {"07"}
            7 -> {"08"}
            8 -> {"09"}
            9 -> {"10"}
            10 -> {"11"}
            else -> {"12"}
        }
        return "$year.$month"
    }

    private fun isLeapYear(year : Int) = when {
        year % 4 == 0 -> {
            when {
                year % 100 == 0 -> year % 400 == 0
                else -> true
            }
        }
        else -> false
    }

    private fun getHourAndMin(ms: Long) : Float {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        return hours.toFloat() + minutes.toFloat()/60F
    }
    private fun getTimeAxisMaxValueFromSpinner() = when(spTimeAxisMaxValueFilter.selectedItemPosition){
                5 -> 4
                4 -> 8
                3 -> 12
                2 -> 16
                1 -> 20
                else -> 24
    }

    private fun addData(){
        viewModel.insertDay(Day("22.01.01",43535L))
        viewModel.insertDay(Day("22.01.02",4356535L))
        viewModel.insertDay(Day("22.01.07",5355L))
        viewModel.insertDay(Day("22.01.15",9345535L))
        viewModel.insertDay(Day("22.01.25",8123535L))
        viewModel.insertDay(Day("20.02.05",9871566L))
        viewModel.insertDay(Day("20.02.06",6549823L))
        viewModel.insertDay(Day("20.02.26",666647L))
        viewModel.insertDay(Day("20.01.01",987562L))
        viewModel.insertDay(Day("20.01.02",6335698L))
        viewModel.insertDay(Day("20.01.03",666L))
        viewModel.insertDay(Day("20.01.04",7985565))
        viewModel.insertDay(Day("22.02.04",79556865))

            viewModel.insertDay(Day("21.11.01",12343L))
            viewModel.insertDay(Day("21.11.06",45468L))
            viewModel.insertDay(Day("21.11.11",984535L))
            viewModel.insertDay(Day("21.11.14",76456L))
            viewModel.insertDay(Day("21.11.28",9995544L))
            viewModel.insertDay(Day("21.11.02",1523431L))
            viewModel.insertDay(Day("21.11.03",42544568L))
            viewModel.insertDay(Day("21.11.04",9845352L))
            viewModel.insertDay(Day("21.11.05",7645466L))
            viewModel.insertDay(Day("21.11.07",9995544L))
            viewModel.insertDay(Day("21.11.08",1234443L))
            viewModel.insertDay(Day("21.11.09",4154568L))
            viewModel.insertDay(Day("21.11.10",9845345L))
            viewModel.insertDay(Day("21.11.12",764560L))
            viewModel.insertDay(Day("21.11.13",15595544L))
            viewModel.insertDay(Day("21.11.15",1234430L))
            viewModel.insertDay(Day("21.11.16",45415468L))
            viewModel.insertDay(Day("21.11.17",9845350L))
            viewModel.insertDay(Day("21.11.18",23534561L))
            viewModel.insertDay(Day("21.11.19",9995544L))
            viewModel.insertDay(Day("21.11.20",122343L))
            viewModel.insertDay(Day("21.11.21",45456418L))
            viewModel.insertDay(Day("21.11.22",984535L))
            viewModel.insertDay(Day("21.11.23",765456L))
            viewModel.insertDay(Day("21.11.24",9995544L))
            viewModel.insertDay(Day("21.11.25",1253423L))
            viewModel.insertDay(Day("21.11.26",4542268L))
            viewModel.insertDay(Day("21.11.27",50455345L))
            viewModel.insertDay(Day("21.11.29",7645163L))
            viewModel.insertDay(Day("21.11.30",9995544L))
    }
}














