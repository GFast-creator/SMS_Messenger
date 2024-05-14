package ru.gfastg98.sms_messenger

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import ru.gfastg98.sms_messenger.SMSBroadcastReceiver.Companion.NEW_MESSAGE_ACTION
import ru.gfastg98.sms_messenger.room.Message
import ru.gfastg98.sms_messenger.room.User
import ru.gfastg98.sms_messenger.ui.theme.colorPool
import java.util.Date


object Repository {
        private const val TAG = "Repository"

        fun refreshSmsInbox(context: Context): SMSTable {
            Log.i(TAG, "refreshSmsInbox: sms is updating")
            val messages = mutableListOf<Message>()
            val users = mutableListOf<User>()

            val cursor =
                context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms._ID,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.TYPE,
                        Telephony.Sms.DATE,
                        Telephony.Sms.THREAD_ID
                    ),
                    null,
                    null
                )
                    ?: return SMSTable()


            val indexId = cursor.getColumnIndex(Telephony.Sms._ID)
            val indexAddress = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val indexBody = cursor.getColumnIndex(Telephony.Sms.BODY)

            val indexType = cursor.getColumnIndex(Telephony.Sms.TYPE)
            val indexDate = cursor.getColumnIndex(Telephony.Sms.DATE)
            val indexThreadId = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)

            if (indexBody < 0 || !cursor.moveToFirst()) return SMSTable()

            do {
                val id = cursor.getLong(indexId)
                val number = cursor.getString(indexAddress)

                val message = cursor.getString(indexBody)
                val type = cursor.getInt(indexType)
                val date = cursor.getLong(indexDate)
                val threadId = cursor.getLong(indexThreadId)

                messages += Message(
                    id = id,
                    text = message,
                    datetime = Date(date),
                    threadId = threadId,
                    type = type
                )

                val isUserExist = users.any { u -> u.num == number }
                if (!isUserExist) {
                    users += User(
                        id = threadId,
                        name = if (number.isNumber)
                            getContactNameFromPhoneNumber(context, number)
                                ?: number
                        else number,
                        num = if (!number.isNumber) {
                            getPhoneNumberFromContactName(context, number) ?: number
                        } else number,
                        color = colorPool[(threadId % 6).toInt()]
                    )
                }
            } while (cursor.moveToNext())

            cursor.close()
            return SMSTable(users, messages)
        }

        fun insertSMS(context: Context, type: Int, address: String, message: String){
            Log.i(TAG, "insertSMS: params: $context, $type, $address, $message")
            try {
                val cv = ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, address)
                    put(Telephony.Sms.BODY, message)
                    put(Telephony.Sms.TYPE, type)
                }

                context.contentResolver.insert(
                    Telephony.Sms.CONTENT_URI,
                    cv
                )
            }catch (e:Exception){
                Log.e(TAG, "insertSMS: ${e.message}", )
            }
        }

        fun sendSMS(context: Context, message: String, address: String, isDigits: Boolean) {
            try {
                val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    100,
                    Intent(context, SMSBroadcastReceiver::class.java)
                        .apply { action = NEW_MESSAGE_ACTION },
                    PendingIntent.FLAG_IMMUTABLE
                )

                val transformedAddress: String = if (isDigits)
                    address.replace(
                        regex = notDigitsAndPlus,
                        replacement = ""
                    )
                else address

                smsManager.sendTextMessage(
                    transformedAddress,
                    null,
                    message,
                    pendingIntent,
                    pendingIntent
                )

                Toast.makeText(
                    context,
                    context.getString(
                        R.string.thread_successfully_sent,
                        transformedAddress,
                        message
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                Log.i(TAG, "sendSMS: message sent to: $transformedAddress, \n message: $message")
            } catch (e: Exception) {
                Toast.makeText(context, "Your sms has failed...", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        @SuppressLint("Range")
        fun getContactList(context: Context): List<User> {
            val cur = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            ) ?: return emptyList()

            val list = mutableListOf<User>()
            if (cur.count > 0) {
                if (cur.moveToFirst()) {
                    do {
                        try {
                            val newUser = User()
                            newUser.id =
                                cur.getLong(
                                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                                )
                            newUser.name =
                                cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                                )
                            if (cur.getInt(
                                    cur.getColumnIndex(
                                        ContactsContract.Contacts.HAS_PHONE_NUMBER
                                    )
                                ) > 0
                            ) {
                                val pCur = context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(newUser.id.toString()),
                                    null
                                ) ?: break
                                pCur.moveToFirst()
                                newUser.num =
                                    pCur.getString(
                                        pCur.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER
                                        )
                                    )

                                pCur.close()
                            }

                            list += newUser
                        } catch (e: Exception) {
                            continue
                        }
                    } while (cur.moveToNext())

                    cur.close()

                }
            }
            return list
        }

        @SuppressLint("Range")
        fun getContactNameFromPhoneNumber(context: Context, number: String): String? {
            Log.i(TAG, "getContactNameFromPhoneNumber: searching: $number")
            var contactName: String? = null
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
                arrayOf(number),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    contactName = it.getString(
                        it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    )
                }
            }
            Log.i(TAG, "getContactNameFromPhoneNumber: founded: $contactName")
            return contactName
        }

        @SuppressLint("Range")
        fun getPhoneNumberFromContactName(context: Context, contactName: String): String? {
            Log.i(TAG, "getPhoneNumberFromContactName: searching: $contactName")
            var phoneNumber: String? = null
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?",
                arrayOf(contactName),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }
            }
            cursor?.close()
            Log.i(TAG, "getContactNameFromPhoneNumber: founded: $phoneNumber")
            return phoneNumber
        }

        @SuppressLint("Range")
        fun deleteThreadFromId(context: Context, threadId: Long) : Int {
            try {
                Log.i(TAG, "Deleting SMS from inbox")

                val v = context.contentResolver.delete(
                    Telephony.Sms.CONTENT_URI,
                    "${Telephony.Sms.THREAD_ID} = ?",
                    arrayOf(
                        threadId.toString()
                    )
                )

                Log.i(TAG, "deleteThreadFromId: $v messages has been deleted")
                return v

            } catch (e: java.lang.Exception) {
                Log.i(TAG, "Could not delete SMS from inbox: " + e.message)
            }
            return 0
        }

        fun deleteMessagesByIds(context: Context, vararg id: Long){
            try {
                val x = context.contentResolver.delete(
                    Telephony.Sms.CONTENT_URI,
                    "${Telephony.Sms._ID} in ${id.joinToString(prefix = "(", postfix = ")")}",

                    null
                )
                Log.i(TAG, "deleteMessagesByIds: $x")
            } catch (e:Exception){
                Log.e(TAG, "deleteMessagesByIds: ${e.message}", )
            }
        }

    }