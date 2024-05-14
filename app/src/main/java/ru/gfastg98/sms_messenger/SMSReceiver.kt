package ru.gfastg98.sms_messenger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import android.util.Log

class SMSBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SMSBroadcastReceiver"
        const val NEW_MESSAGE_ACTION = "NEW_MESSAGE"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        Log.i(TAG, "onReceive: broadcast received: ${intent.action}")


        //Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()
        when (intent.action) {
            SMS_RECEIVED_ACTION, NEW_MESSAGE_ACTION -> {
                /*val bundle = intent.extras
                if (bundle != null) {
                    // get sms objects
                    val pduArray = (bundle["pdus"] as Array<*>?)!!
                    if (pduArray.isEmpty()) {
                        return
                    }
                    // large message might be broken into many
                    val messages = mutableListOf<SmsMessage>()

                    val sb = StringBuilder()
                    for (i in pduArray.indices) {
                        val smsMessage = SmsMessage.createFromPdu(pduArray[i] as ByteArray, bundle.getString("format"))
                        messages += smsMessage
                        sb.append(smsMessage.messageBody)
                    }

                    val sender = messages[0].originatingAddress
                    val message = sb.toString()



                }*/
                Log.i(TAG, "onReceive: mainActivity is: ${MessengerViewModel.instance}")
                //MainActivity.instance?.updateSms()
                MessengerViewModel.instance?.doCommand<Nothing>(Commands.UPDATE_SMS, context)
                abortBroadcast()
            }
        }
    }
}

class SMSBroadcastReceiver1 : BroadcastReceiver() {//TODO: это заглушка для MMS
    companion object {
        private const val TAG = "SMSBroadcastReceiver"
        const val NEW_MESSAGE_ACTION = "NEW_MESSAGE"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        Log.i(TAG, "onReceive: broadcast received: ${intent.action}")


        //Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()
        when (intent.action) {
            SMS_RECEIVED_ACTION, NEW_MESSAGE_ACTION -> {
                /*val bundle = intent.extras
                if (bundle != null) {
                    // get sms objects
                    val pduArray = (bundle["pdus"] as Array<*>?)!!
                    if (pduArray.isEmpty()) {
                        return
                    }
                    // large message might be broken into many
                    val messages = mutableListOf<SmsMessage>()

                    val sb = StringBuilder()
                    for (i in pduArray.indices) {
                        val smsMessage = SmsMessage.createFromPdu(pduArray[i] as ByteArray, bundle.getString("format"))
                        messages += smsMessage
                        sb.append(smsMessage.messageBody)
                    }

                    val sender = messages[0].originatingAddress
                    val message = sb.toString()



                }*/
                Log.i(TAG, "onReceive: mainActivity is: ${MessengerViewModel.instance}")
                //MainActivity.instance?.updateSms()
                MessengerViewModel.instance?.doCommand<Nothing>(Commands.UPDATE_SMS, context)
                abortBroadcast()
            }
        }
    }
}