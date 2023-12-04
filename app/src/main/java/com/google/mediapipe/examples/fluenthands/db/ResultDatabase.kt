package com.google.mediapipe.examples.fluenthands.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Result::class], version=2)
abstract class ResultDatabase: RoomDatabase() {
    abstract val resultDatabaseDao: ResultDatabaseDao

    companion object {
        @Volatile private var INSTANCE: ResultDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        fun getInstance(context: Context): ResultDatabase {
            var instance = INSTANCE
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, ResultDatabase::class.java, "results_table")
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
            }
            return instance
        }
    }
}