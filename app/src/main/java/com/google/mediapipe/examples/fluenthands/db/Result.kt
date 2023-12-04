package com.google.mediapipe.examples.fluenthands.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Calendar


@Entity (tableName = "results_table")
@TypeConverters(Converters::class)
data class Result (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "score")
    var score: String = "",

    @ColumnInfo(name = "date_time")
    var dateTime: Calendar

    )


