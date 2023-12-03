package com.google.mediapipe.examples.fluenthands.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData


class ResultViewModel(private var repository: ResultRepository): ViewModel() {
    var allResultData: LiveData<List<Result>> = repository.allResults.asLiveData()

    fun insertResult(result: Result) {
        repository.insertResult(result)
    }
    fun getResultById(resultId: Long): LiveData<Result> {
        return repository.getResultById(resultId)
    }
    fun deleteResultById(resultId: Long) {
        repository.deleteResultById(resultId)
    }

    fun deleteResultTable() {
        repository.deleteResultTable()
    }
}