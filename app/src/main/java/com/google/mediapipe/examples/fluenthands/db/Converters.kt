package com.google.mediapipe.examples.fluenthands.db

import androidx.room.TypeConverter

import java.util.Calendar

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let { Calendar.getInstance().apply { timeInMillis = it } }
    }

    @TypeConverter
    fun toTimestamp(timestamp: Calendar?): Long? {
        return timestamp?.timeInMillis
    }
}
