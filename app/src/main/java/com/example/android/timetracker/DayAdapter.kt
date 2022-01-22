package com.example.android.timetracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.android.timetracker.Constants.EMPTY_NOTE
import kotlinx.android.synthetic.main.item_day.view.*

class DayAdapter(
        private val listener : IDayAdapter
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>(){

    inner class DayViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val dayItem: ConstraintLayout = itemView.plItemDay
    }

    val diffCallback = object : DiffUtil.ItemCallback<Day>() {
        override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.dateOfDayYYMMDD == newItem.dateOfDayYYMMDD
        }

        override fun areContentsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list : List<Day>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val viewHolder =DayViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_day,
                        parent,
                        false
                )
        )
        viewHolder.dayItem.setOnClickListener{
            val position = viewHolder.adapterPosition
            listener.showDetailsDialog(differ.currentList[position], position)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = differ.currentList[position]
        holder.itemView.apply {
            tvDateItem.text = TimerUtility.reverseDate(day.dateOfDayYYMMDD)
            tvTimeItem.text = TimerUtility.getFormattedStopWatchTime(day.studyDurationOfDayInMillis)
            if(day.noteOfTheDay == ""){
                tvNoteItem.text = EMPTY_NOTE
                tvNoteItem.setTextColor(Color.GRAY)
            }
            else{tvNoteItem.text = day.noteOfTheDay}

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}
interface IDayAdapter{
    fun showDetailsDialog(day : Day, position : Int)
}