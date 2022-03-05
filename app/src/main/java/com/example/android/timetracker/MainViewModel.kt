package com.example.android.timetracker

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel @ViewModelInject constructor(
        val mainRepository: MainRepository
) : ViewModel(){

//    private val daysSortedByDate = mainRepository.getAllDaysSortedByDate()
//    private val daysSortedByTimeInMillis = mainRepository.getAllDaysSortedByTime()
    val currDay =
            mainRepository.getDayWithDate(SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date()))

    fun getDaysOfMonthYYmmSortedByDateASC(month : String) = mainRepository.getDaysOfMonthYYmmSortedByDateASC(month)
    fun getDaysOfMonthYYmmSortedByDateDESC(month : String) = mainRepository.getDaysOfMonthYYmmSortedByDateDESC(month)
    fun getDaysOfMonthYYmmSortedByTime(month : String) = mainRepository.getDaysOfMonthYYmmSortedByTime(month)
    fun getAllDataSortedByDate() = mainRepository.getAllDaysSortedByDate()
    fun getAllDataSortedByTime() = mainRepository.getAllDaysSortedByTime()


    fun getDaysOfYear(year: String) = mainRepository.getDaysOfYear(year)

    fun insertDay(day : Day) = viewModelScope.launch {
        mainRepository.insertDay(day)
    }
    fun updateDay(day : Day) = viewModelScope.launch {
        mainRepository.updateDay(day)
    }
    fun deleteDayWithDate(dateProvided : String) = viewModelScope.launch {
        mainRepository.deleteDayWithDate(dateProvided)
    }
    fun deleteAllDatabase() = viewModelScope.launch {
        mainRepository.deleteAllDatabase()
    }

    val days= MediatorLiveData<List<Day>>()
//    var sortType = SortType.DATE
//    var monthYYmm = "All"

//    init {
//            days.addSource(daysSortedByDate){ result ->
//                if(sortType == SortType.DATE && monthYYmm == "All"){
//                    result?.let { days.value = it }
//                }
//            }
//            days.addSource(daysSortedByTimeInMillis){ result ->
//                if(sortType == SortType.STUDY_TIME && monthYYmm == "All"){
//                    result?.let { days.value = it }
//                }
//            }
//            days.addSource(getDaysOfMonthYYmmSortedByDateDESC(monthYYmm)){ result ->
//                if(sortType == SortType.DATE && monthYYmm != "All"){
//                    result?.let { days.value = it }
//                }
//            }
//            days.addSource(getDaysOfMonthYYmmSortedByTime(monthYYmm)){result ->
//                if(sortType == SortType.STUDY_TIME && monthYYmm != "All"){
//                    result?.let { days.value = it }
//                }
//            }
//    }

    fun sortDays(sortType: SortType, monthYYmm : String) = when(sortType){
        SortType.DATE -> {
            if(monthYYmm == "All"){
                days.addSource(getAllDataSortedByDate()){result ->
                    result?.let { days.value = it }
                }
            }
            else {
                days.addSource(getDaysOfMonthYYmmSortedByDateDESC(monthYYmm)){result ->
                    result?.let { days.value = it }
                }
            }
        }
        SortType.STUDY_TIME -> {
            if(monthYYmm == "All"){
                days.addSource(getAllDataSortedByTime()){result ->
                    result?.let { days.value = it }
                }
            }
            else {
                days.addSource(getDaysOfMonthYYmmSortedByTime(monthYYmm)){result ->
                    result?.let { days.value = it }
                }
            }
        }
    }
//            .also {
//        this.sortType = sortType
//        this.monthYYmm = monthYYmm
//    }
}














