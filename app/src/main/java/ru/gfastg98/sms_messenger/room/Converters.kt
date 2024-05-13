package ru.gfastg98.sms_messenger.room

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import ru.gfastg98.sms_messenger.color
import ru.gfastg98.sms_messenger.string
import java.util.Date

@ProvidedTypeConverter
class Converters {
    val TAG = "conv"
    /// Color - String
    @TypeConverter
    fun colorFromString(value: String?): Color {
        if (value == null || value == "0") return Color(0xFF3F51B5)
        Log.i (TAG, "color parsed: ${value.color}")
        return value.color
    }

    @TypeConverter
    fun colorToString(color: Color?): String {
        if (color == null) return "0"
        val c = color.string
        Log.i(TAG, "colorToString: $c")
        return c
    }
    ///

    /// Date - Long
    @TypeConverter
    fun colorFromString(value: Long?): Date? {
        return value?.let { Date(value) }?: Date()
    }

    @TypeConverter
    fun colorToString(date: Date?): Long? {
        return date?.time?:0
    }
    ///
}