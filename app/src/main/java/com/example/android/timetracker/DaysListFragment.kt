package com.example.android.timetracker

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.timetracker.Constants.EMPTY_NOTE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_day_details.*
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.android.synthetic.main.fragment_days_list.*
import kotlinx.android.synthetic.main.fragment_timer.*

@AndroidEntryPoint
class DaysListFragment : Fragment(R.layout.fragment_days_list), IDayAdapter {

    private val viewModel : MainViewModel by viewModels()
    private lateinit var dayAdapter: DayAdapter
    private lateinit var sharedPrefsDLF : SharedPreferences
    private lateinit var editorForSpinnerPrefDLF : SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "Statistics"
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPrefsDLF = requireActivity().getSharedPreferences("sortingPref",Context.MODE_PRIVATE)
        editorForSpinnerPrefDLF = sharedPrefsDLF?.edit()
        setupViewsDLF()
//        when(sharedPrefsDLF.getBoolean("filterPref",false)){
//            true -> viewModel.monthYYmm = getYYmmStringFromSpinners()
//            false -> viewModel.monthYYmm = "All"
//        }
//        when(sharedPrefsDLF.getInt("sortType",0)){
//            0 -> viewModel.sortType = SortType.DATE
//            1 -> viewModel.sortType = SortType.STUDY_TIME
//        }
        setupRecyclerView()
        subscribeToClickable()
        util()
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
            tvTotalTime.text = TimerUtility.getFormattedStopWatchTime(sumTimeOfDays(it))
        })
    }

    private fun subscribeToClickable(){
        stcFilterDLF.setOnCheckedChangeListener { buttonView, isChecked ->
            filterAssociatedViewsVisibility(isChecked)
        }
        btnApplyDLF.setOnClickListener {
            onDLFApplyButtonClicked()
        }
    }
    private fun filterAssociatedViewsVisibility(visible : Boolean){
        if(visible){
            spYearFilterDLF.visibility = View.VISIBLE
            spMonthFilterDLF.visibility = View.VISIBLE
        }else {
            spYearFilterDLF.visibility = View.GONE
            spMonthFilterDLF.visibility = View.GONE
        }
    }

    private fun setupViewsDLF(){
        when(sharedPrefsDLF.getInt("sortType",0)){
            0 -> spSortTypeFilterDLF.setSelection(0)
            1 -> spSortTypeFilterDLF.setSelection(1)
        }

        spYearFilterDLF.adapter = context?.let {
            ArrayAdapter(it, R.layout.support_simple_spinner_dropdown_item, MainActivity.chartYearSpinnerList.reversed())
        }
        when(sharedPrefsDLF.getBoolean("filterPref",false)){
            true -> {
                        stcFilterDLF.isChecked = true
                        filterAssociatedViewsVisibility(true)
                    }
            false -> {
                        stcFilterDLF.isChecked = false
                        filterAssociatedViewsVisibility(false)
                    }
        }
        when(sharedPrefsDLF.getInt("yearPrefDLF",0)){
            2 -> {
                if(spYearFilterDLF.count > 2)
                    spYearFilterDLF.setSelection(2)
                else spYearFilterDLF.setSelection(0)
            }
            1 -> {
                if(spYearFilterDLF.count > 1)
                    spYearFilterDLF.setSelection(1)
                else spYearFilterDLF.setSelection(0)
            }
            else -> spYearFilterDLF.setSelection(0)
        }
        when(sharedPrefsDLF.getInt("monthPrefDLF",0)){
            0 -> spMonthFilterDLF.setSelection(0)
            1 -> spMonthFilterDLF.setSelection(1)
            2 -> spMonthFilterDLF.setSelection(2)
            3 -> spMonthFilterDLF.setSelection(3)
            4 -> spMonthFilterDLF.setSelection(4)
            5 -> spMonthFilterDLF.setSelection(5)
            6 -> spMonthFilterDLF.setSelection(6)
            7 -> spMonthFilterDLF.setSelection(7)
            8 -> spMonthFilterDLF.setSelection(8)
            9 -> spMonthFilterDLF.setSelection(9)
            10 -> spMonthFilterDLF.setSelection(10)
            11 -> spMonthFilterDLF.setSelection(11)
        }
    }

    private fun onDLFApplyButtonClicked(){
        util()

        editorForSpinnerPrefDLF.apply{
            putBoolean("filterPref", stcFilterDLF.isChecked)
            putInt("sortType", spSortTypeFilterDLF.selectedItemPosition)
            putInt("yearPrefDLF", spYearFilterDLF.selectedItemPosition)
            putInt("monthPrefDLF", spMonthFilterDLF.selectedItemPosition)
            apply()
        }
    }
    private fun util(){
        if(!stcFilterDLF.isChecked){
            if(spSortTypeFilterDLF.selectedItemPosition == 0){
                viewModel.sortDays(SortType.DATE, "All")
            }else{
                viewModel.sortDays(SortType.STUDY_TIME, "All")
            }
            tvMonthYYDLF.text = "All Data"
        }else{
            val yyMM = getYYmmStringFromSpinners()
            if(spSortTypeFilterDLF.selectedItemPosition == 0){
                viewModel.sortDays(SortType.DATE, yyMM)
            }else{
                viewModel.sortDays(SortType.STUDY_TIME, yyMM)
            }
            tvMonthYYDLF.text = TimerUtility.getMonthYY(yyMM)
        }
    }
    private fun getYYmmStringFromSpinners() : String {
        val year =  spYearFilterDLF.selectedItem.toString().substring(2,4)
        val month = when(spMonthFilterDLF.selectedItemPosition){
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
    private fun sumTimeOfDays(list : List<Day>) : Long{
        var res = 0L
        for(day in list){
            res += day.studyDurationOfDayInMillis
        }
        return res
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