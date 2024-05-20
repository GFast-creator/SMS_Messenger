package ru.gfastg98.sms_messenger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.provider.Telephony.Sms.Intents.SMS_DELIVER_ACTION
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import android.telephony.SmsMessage
import android.util.Log
import ru.gfastg98.sms_messenger.Command.INSERT_SMS
import ru.gfastg98.sms_messenger.Command.SEND_NOTIFICATION

class SMSBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SMSBroadcastReceiver"
        const val NEW_MESSAGE_ACTION = "NEW_MESSAGE"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        Log.i(TAG, "onReceive: broadcast received: ${intent.action}")
        context?:let {
            Log.e(TAG, "onReceive: context is null")
            return
        }

        when (intent.action) {
            SMS_DELIVER_ACTION, NEW_MESSAGE_ACTION -> {
                val bundle = intent.extras
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
                        val smsMessage = SmsMessage.createFromPdu(
                            pduArray[i] as ByteArray,
                            bundle.getString("format")
                        )
                        messages += smsMessage
                        sb.append(smsMessage.messageBody)
                    }

                    val sender = messages[0].originatingAddress?: context.getString(R.string.message)
                    val message = sb.toString()

                    Log.d(TAG, "onReceive: viewmodel is: ${MessengerViewModel.instance}")

                    MessengerViewModel.instance?.run {
                        onEvent<Unit>(
                            INSERT_SMS(
                                context,
                                Telephony.Sms.MESSAGE_TYPE_INBOX,
                                sender,
                                message
                            )
                        )

                        onEvent<Unit>(
                            SEND_NOTIFICATION(
                            context,
                            sender,
                            message
                            )
                        )
                    }
                }
                abortBroadcast()
            }
        }
    }
}

class MMSBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SMSBroadcastReceiver"
        const val NEW_MESSAGE_ACTION = "NEW_MESSAGE"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        Log.i(TAG, "onReceive: broadcast received: ${intent.action}")


        //Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()
        when (intent.action) {
            SMS_RECEIVED_ACTION, NEW_MESSAGE_ACTION -> {

                Log.i(TAG, "onReceive: mainActivity is: ${MessengerViewModel.instance}")
                //MainActivity.instance?.updateSms()
                //MessengerViewModel.instance?.onEvent<Unit>(UPDATE_SMS(context))
                abortBroadcast()
            }
        }
    }
}