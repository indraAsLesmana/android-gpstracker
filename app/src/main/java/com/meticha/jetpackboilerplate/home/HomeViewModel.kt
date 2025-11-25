package com.meticha.jetpackboilerplate.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workManager: androidx.work.WorkManager
) : ViewModel() {
    init {
        println("init started of home")
    }

    fun fetchAndSendLocationImmediately() {
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.meticha.jetpackboilerplate.workers.LocationWorker>()
            .build()
        workManager.enqueue(workRequest)
    }

    override fun onCleared() {
        println("init cleared of home")
        super.onCleared()
    }
}