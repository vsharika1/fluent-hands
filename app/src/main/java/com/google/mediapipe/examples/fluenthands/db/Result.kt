package com.google.mediapipe.examples.fluenthands.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity (tableName = "results_table")
data class Result (
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,

    )