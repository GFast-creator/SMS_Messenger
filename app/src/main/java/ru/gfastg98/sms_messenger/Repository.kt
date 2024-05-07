package ru.gfastg98.sms_messenger

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Toast
import ru.gfastg98.sms_messenger.ui.theme.colorPool
import java.util.Date

class Repository {
    companion object{
        fun refreshSmsInbox(context: Context): SMSTable {
            val users = mutableListOf<User>()
            val smsList = mutableListOf<Message>()
            val cursor =
                context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.TYPE,
                        Telephony.Sms.DATE,
                    ),
                    null,
                    null
                )
                    ?: return (emptyList<User>() to emptyList())

            val indexBody = cursor.getColumnIndex(Telephony.Sms.BODY)
            val indexAddress = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val indexType = cursor.getColumnIndex(Telephony.Sms.TYPE)
            val indexDate = cursor.getColumnIndex(Telephony.Sms.DATE)

            if (indexBody < 0 || !cursor.moveToFirst()) return (emptyList<User>() to emptyList())

            do {
                val username = cursor.getString(indexAddress)
                val message = cursor.getString(indexBody)
                val type = cursor.getInt(indexType)
                val date = cursor.getLong(indexDate)

                val userIndex = users.indexOfFirst { u -> u.name == username }
                if (userIndex != -1) {
                    smsList += Message(
                        text = message,
                        datetime = Date(date),
                        userId = users[userIndex].id,
                        type = type
                    )
                } else {
                    val newUser = User((users.lastOrNull()?.id?.plus(1)) ?: 0, name = username)
                    newUser.color = colorPool[newUser.id % 6]
                    users += newUser
                    smsList += Message(
                        text = message,
                        datetime = Date(date),
                        userId = newUser.id,
                        type = type
                    )
                }
            } while (cursor.moveToNext())

            cursor.close()

            return users to smsList
        }

        fun sendSMS(context : Context, message: String, to: String) {
            try {
                val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    100,
                    Intent(context, SMSBroadcastReceiver::class.java)
                        .apply { action = "NEW_MESSAGE" },
                    PendingIntent.FLAG_IMMUTABLE
                )

                smsManager.sendTextMessage(
                    to,
                    null,
                    message,
                    pendingIntent,
                    pendingIntent
                )

                Toast.makeText(context, "Your sms has successfully sent!", Toast.LENGTH_SHORT).show()
            } catch (e : Exception){
                Toast.makeText(context,"Your sms has failed...", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}