package com.example.android.timetracker

import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.timetracker.Constants.EMPTY_NOTE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_day_details.*
import kotlinx.android.synthetic.main.fragment_days_list.*
import kotlinx.android.synthetic.main.fragment_timer.*

@AndroidEntryPoint
class DaysListFragment : Fragment(R.layout.fragment_days_list), IDayAdapter {

    private val viewModel : MainViewModel by viewModels()
    private lateinit var dayAdapter: DayAdapter

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        val sharedPrefForSorting = this.activity?.getSharedPreferences("sortingPref",Context.MODE_PRIVATE)
        val editorForSortingPref = sharedPrefForSorting?.edit()
        when(sharedPrefForSorting?.getInt("sortType",99)){
            0 -> viewModel.sortType = SortType.DATE
            1 -> viewModel.sortType = SortType.STUDY_TIME
        }

        when(viewModel.sortType){
            SortType.DATE -> spDayListFilter.setSelection(0)
            SortType.STUDY_TIME -> spDayListFilter.setSelection(1)
        }
        spDayListFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0 -> {
                        viewModel.sortDays(SortType.DATE)
                        editorForSortingPref?.apply {
                            putInt("sortType",0)
                            apply()
                        }
                    }
                    1 -> {
                        viewModel.sortDays(SortType.STUDY_TIME)
                        editorForSortingPref?.apply {
                            putInt("sortType",1)
                            apply()
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        subscribeToObservers()
    }

    private fun setupRecyclerView() = rvDays.apply {
        dayAdapter = DayAdapter(this@DaysListFragment)
        adapter = dayAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        viewModel.days.observe(viewLifecycleOwner, {
            dayAdapter.submitList(it)
            it?.let {
               tvTotalDays.text = it.size.toString()
            }
        })
        viewModel.totalTimeOfStudy.observe(viewLifecycleOwner, Observer {
            it?.let {
                tvTotalTime.text = TimerUtility.getFormattedStopWatchTime(it)
            }
        })
    }

    override fun showDetailsDialog(day: Day, position : Int) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_day_details,null)
        val tvDate = dialogLayout.findViewById<TextView>(R.id.tvDateDetailDialog)
        val tvTime = dialogLayout.findViewById<TextView>(R.id.tvTimeDetailDialog)
        val noteTextView = dialogLayout.findViewById<TextView>(R.id.tvNoteDetailDialog)
        val noteEditText = dialogLayout.findViewById<EditText>(R.id.etEditNoteDetailDialog)
        val editImg = dialogLayout.findViewById<ImageView>(R.id.ivEditNoteDetailDialog)
        val doneImg = dialogLayout.findViewById<ImageView>(R.id.ivDoneDetailDialog)
        tvDate.text = TimerUtility.reverseDate(day.dateOfDayYYMMDD)
        tvTime.text = TimerUtility.getFormattedStopWatchTime(day.studyDurationOfDayInMillis)
        if(day.noteOfTheDay == ""){
            noteTextView.text = EMPTY_NOTE
            noteTextView.gravity = Gravity.CENTER
        }
        else{noteTextView.text = day.noteOfTheDay}
        noteTextView.movementMethod = ScrollingMovementMethod()

        editImg.setOnClickListener {
            onDetailDialogEditNoteButtonClicked(day, noteTextView, noteEditText, editImg, doneImg)
        }
        with(builder){
            setOnCancelListener{
                dayAdapter.notifyItemChanged(position)
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun onDetailDialogEditNoteButtonClicked(
        day: Day, noteTextView : TextView, noteEditText : EditText,
        editImg : ImageView, doneImg : ImageView
    ){
        noteTextView.visibility = View.INVISIBLE
        editImg.visibility = View.INVISIBLE
        noteEditText.visibility = View.VISIBLE
        doneImg.visibility = View.VISIBLE

        noteEditText.setText(day.noteOfTheDay)

        noteEditText.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(noteEditText, 0)
        noteEditText.setSelection(noteEditText.length())

        doneImg.setOnClickListener {

            day.noteOfTheDay = noteEditText.text.toString()
            viewModel.updateDay(day)
            if(noteEditText.text.toString() == ""){
                noteTextView.text = EMPTY_NOTE
                noteTextView.gravity = Gravity.CENTER
            }else {
                noteTextView.text = noteEditText.text
                noteTextView.gravity = Gravity.NO_GRAVITY
            }
            noteEditText.visibility = View.INVISIBLE
            doneImg.visibility = View.INVISIBLE
            noteTextView.visibility = View.VISIBLE
            editImg.visibility = View.VISIBLE
            imm.hideSoftInputFromWindow(noteEditText.windowToken,0)
        }
    }
}