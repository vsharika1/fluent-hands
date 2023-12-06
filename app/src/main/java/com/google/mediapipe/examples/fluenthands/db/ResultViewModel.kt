package com.google.mediapipe.examples.fluenthands.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import java.lang.IllegalArgumentException


class ResultViewModel(private var repository: ResultRepository): ViewModel() {
    var allResultData: LiveData<List<Result>> = repository.allResults.asLiveData()

    fun insertResult(result: Result) {
        repository.insertResult(result)
    }

}


class ResultViewModelFactory (private val repository: ResultRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ 
        if(modelClass.isAssignableFrom(ResultViewModel::class.java))
            return ResultViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}