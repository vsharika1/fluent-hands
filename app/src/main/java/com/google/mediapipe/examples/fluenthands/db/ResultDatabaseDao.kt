package com.google.mediapipe.examples.fluenthands.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao interface ResultDatabaseDao {
    @Insert
    suspend fun addResult(result: Result)

    @Query("SELECT * FROM results_table ORDER BY id ASC") fun getAllResults(): Flow<List<Result>>
    @Query("SELECT * FROM results_table WHERE id= :resultId") fun getResultById(resultId: Long): LiveData<Result>
    @Query("DELETE FROM results_table") suspend fun deleteAll()
    @Query("DELETE FROM results_table WHERE id = :resultId") suspend fun deleteExerciseById(resultId: Long)


}