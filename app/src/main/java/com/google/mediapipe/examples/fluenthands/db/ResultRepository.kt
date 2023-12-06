package com.google.mediapipe.examples.fluenthands.db

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ResultRepository(private val resultDatabaseDao: ResultDatabaseDao) {
    var allResults: Flow<List<Result>> = resultDatabaseDao.getAllResults()

    fun insertResult(result: Result) {
        CoroutineScope(Dispatchers.IO).launch {
            resultDatabaseDao.addResult(result)
        }

    }
}