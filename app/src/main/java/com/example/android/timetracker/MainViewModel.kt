package com.example.android.timetracker

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel @ViewModelInject constructor(
        val mainRepository: MainRepository
) : ViewModel(){


    private val daysSortedByDate = mainRepository.getAllDaysSortedByDate()
    private val daysSortedByTimeInMillis = mainRepository.getAllDaysSortedByTime()
    val currDay =
            mainRepository.getDayWithDate(SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date()))
    val totalTimeOfStudy = mainRepository.getTotalStudyTimeInMillis()

    fun getDaysOfMonthYYmm(month : String) = mainRepository.getDaysOfMonthYYmm(month)
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
    var sortType = SortType.DATE

    init {
        days.addSource(daysSortedByDate){ result ->
            if(sortType == SortType.DATE){
                result?.let { days.value = it }
            }
        }
        days.addSource(daysSortedByTimeInMillis){ result ->
            if(sortType == SortType.STUDY_TIME){
                result?.let { days.value = it }
            }
        }
    }

    fun sortDays(sortType: SortType) = when(sortType){
        SortType.DATE -> daysSortedByDate.value?.let { days.value = it }
        SortType.STUDY_TIME -> daysSortedByTimeInMillis.value?.let { days.value = it }
    }.also {
        this.sortType = sortType
    }

}














