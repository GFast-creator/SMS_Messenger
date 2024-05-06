package ru.gfastg98.sms_messenger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast


class SMSBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        Log.i(TAG, "onReceive: broadcast received")

        Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()
        if (intent.action == SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            if (bundle != null) {
                // get sms objects
                val pduArray = (bundle["pdus"] as Array<*>?)!!
                if (pduArray.isEmpty()) {
                    return
                }
                // large message might be broken into many
//                val messages = arrayOfNulls<SmsMessage>(
//                    pduArray.size
//                )
                var messages = mutableListOf<SmsMessage>()

                val sb = StringBuilder()
                for (i in pduArray.indices) {
                    val smsMessage = SmsMessage.createFromPdu(pduArray[i] as ByteArray, bundle.getString("format"))
                    messages += smsMessage
                    sb.append(smsMessage.messageBody)
                }
                val sender = messages[0].originatingAddress
                val message = sb.toString()
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                // prevent any other broadcast receivers from receiving broadcast

                Log.i(TAG, "onReceive: mainActivity is: ${MainActivity.instance}")
                MainActivity.instance?.updateSms()
                abortBroadcast()
                Log.i(TAG, "onReceive: message is: $message")
            }
        }
    }

    companion object {
        private const val TAG = "SMSBroadcastReceiver"
    }
}