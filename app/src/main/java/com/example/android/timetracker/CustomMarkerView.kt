package com.example.android.timetracker

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*

class CustomMarkerView (
        private val YYmmString : String,
        private val days: List<Day>,
        c : Context,
        layoutId : Int
        )  : MarkerView(c, layoutId){

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e == null)return

        val ddInDate = e.x.toInt()
        val dateFormed = formDate(ddInDate, YYmmString)
        val ind = linearSearchInDayList(dateFormed)

        if(ind == -1){
            tvMarkerDate.text = TimerUtility.reverseDate(dateFormed)
            tvMarkerTime.text = "00:00:00"
        } else {
            val day = days[ind]
            tvMarkerDate.text = TimerUtility.reverseDate(day.dateOfDayYYMMDD)
            tvMarkerTime.text = TimerUtility.getFormattedStopWatchTime(day.studyDurationOfDayInMillis)
        }
    }

    private fun linearSearchInDayList(dateFormed: String): Int {
        for(i in days.indices){
            if(days[i].dateOfDayYYMMDD == dateFormed)return i
        }
        return -1
    }

    private fun formDate(ddInDate: Int, YYmmString: String): String {
        var ddString = ddInDate.toString()
        if(ddString.length == 1) ddString = "0$ddString"
        return "$YYmmString.$ddString"
    }

}