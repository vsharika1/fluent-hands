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

    fun getResultById(resultId: Long): LiveData<Result> {
        return resultDatabaseDao.getResultById(resultId)
    }

    fun deleteResultTable() {
        CoroutineScope(Dispatchers.IO).launch {
            resultDatabaseDao.deleteAll()
        }
    }

    fun deleteResultById(resultId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            resultDatabaseDao.deleteExerciseById(resultId)
        }
    }

    fun deleteAll(){
        CoroutineScope(Dispatchers.IO).launch {
            resultDatabaseDao.deleteAll()
        }
    }
}