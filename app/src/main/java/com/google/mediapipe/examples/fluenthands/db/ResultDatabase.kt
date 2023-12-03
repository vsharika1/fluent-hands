package com.google.mediapipe.examples.fluenthands.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Result::class], version=1)
abstract class ResultDatabase: RoomDatabase() {
    abstract val resultDatabaseDao: ResultDatabaseDao

    companion object {
        @Volatile private var INSTANCE: ResultDatabase? = null

        fun getInstance(context: Context): ResultDatabase {
            var instance = INSTANCE
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, ResultDatabase::class.java, "results_table")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
            }
            return instance
        }
    }
}