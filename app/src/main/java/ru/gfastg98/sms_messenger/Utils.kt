package ru.gfastg98.sms_messenger

import android.util.Log
import androidx.compose.ui.graphics.Color
import java.util.Date

val Date.isToday: Boolean
    get() = date == Date().date &&
            month == Date().month &&
            year == Date().year

val String.color: Color
    get() =
        try {
            Color(android.graphics.Color.parseColor("#$this"))
        } catch (e: Exception) {
            e.message?.let { Log.e("parser", it) }
            Color(0)
        }

val Color.string: String
    get() =
        try {
            val red = this.red * 255
            val green = this.green * 255
            val blue = this.blue * 255
            String.format("%02x%02x%02x", red.toInt(), green.toInt(), blue.toInt())
        } catch (e: Exception) {
            e.message?.let { Log.e("parser", it) }
            "0"
        }

val String.isNumber
    get() = matches(Regex(pattern = "^[0-9+]+$")) && length in arrayOf(9,10)

val String.checkColor : Boolean
    get() {
        val b1 = this.length in intArrayOf(6, 8)
        val b2 = this.lowercase().all { "0123456789abcdef".contains(it) }
        Log.i("checkColor", "checkColor: ${!(b1 && b2)}")
        return !(b1 && b2)
    }