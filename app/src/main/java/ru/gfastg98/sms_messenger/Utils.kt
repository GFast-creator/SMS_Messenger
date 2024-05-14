package ru.gfastg98.sms_messenger

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
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

val notDigitsAndPlus
    get() = Regex(pattern = "[^0-9+]")
val onlyDigitsAndPlus
    get() = Regex(pattern = "^[0-9+]+$")
val String.isNumber
    get() = matches(onlyDigitsAndPlus)
            && ((startsWith("+") && count{c -> c == '+'} == 1) || count{c -> c == '+'} == 0)
            || isEmpty() /*&& length in arrayOf(9,10)*/


val CharactersForHEX
    get() = "0123456789abcdef"
val String.checkColor: Boolean
    get() {
        val b1 = this.length in intArrayOf(6, 8)
        val b2 = this.lowercase().all { CharactersForHEX.contains(it) }
        Log.i("checkColor", "checkColor: ${!(b1 && b2)}")
        return !(b1 && b2)
    }

val Int.messageTypeName: String
    @Composable
    get() {
        return when (this) {
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX -> stringResource(R.string.type_inbox)
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT -> stringResource(R.string.type_sent)
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT -> stringResource(R.string.type_draft)
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX -> stringResource(R.string.type_outbox)
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED -> stringResource(R.string.type_failed)
            Telephony.TextBasedSmsColumns.MESSAGE_TYPE_QUEUED -> stringResource(R.string.type_queued)
            else -> "error"
        }
    }

val Int.messageTypeIcon: ImageVector
    get() {
        return when (this) {
            Telephony.Sms.MESSAGE_TYPE_INBOX -> Icons.Rounded.Inbox
            Telephony.Sms.MESSAGE_TYPE_FAILED -> Icons.Rounded.ErrorOutline
            Telephony.Sms.MESSAGE_TYPE_OUTBOX -> Icons.Rounded.Check
            Telephony.Sms.MESSAGE_TYPE_SENT -> Icons.Rounded.DoneAll
            Telephony.Sms.MESSAGE_TYPE_DRAFT -> Icons.Rounded.Edit
            else -> Icons.Rounded.Sync
        }
    }

@ExperimentalCoroutinesApi
fun ContentResolver.register(uri: Uri) = callbackFlow {
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            trySend(selfChange)
        }
    }

    registerContentObserver(uri, true, observer)

    awaitClose  {
        unregisterContentObserver(observer)
    }
}