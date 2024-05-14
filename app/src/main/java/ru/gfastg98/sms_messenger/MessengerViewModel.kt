package ru.gfastg98.sms_messenger

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gfastg98.sms_messenger.activites.StartActivity
import ru.gfastg98.sms_messenger.room.Message
import ru.gfastg98.sms_messenger.room.User
import javax.inject.Inject

enum class Commands {
    //GET_USERS, GET_MESSAGES, GET_LAST_MESSAGE,

    //INSERT_USER, DELETE_USER, DELETE_USERS,
    //INSERT_MESSAGE, DELETE_MESSAGE, DELETE_MESSAGES,
    SWITCH_DIALOG_ON, SWITCH_DIALOG_OFF,

    DELETE_LIST_USERS_UPDATE, DELETE_LIST_USERS_PLUS, DELETE_LIST_USERS_MINUS,
    DELETE_LIST_MESSAGES_UPDATE, DELETE_LIST_MESSAGES_PLUS, DELETE_LIST_MESSAGES_MINUS,

    UPDATE_SMS, SEND_SMS, ADD_USER, GET_CONTACTS,

    INSERT_SMS,

    DELETE_THREADS, DELETE_MESSAGES,

    SEND_NOTIFICATION
}

data class SMSTable(
    var users: List<User> = emptyList(),
    var messages: List<Message> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessengerViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    companion object {
        const val TAG = "MessengerViewModel"
        var instance: MessengerViewModel? = null
    }

    @Inject lateinit var vibrator: Vibrator
    @Inject lateinit var notificationManager: NotificationManager

    private val _isDialog = MutableStateFlow(false)
    val isDialogStateFlow: StateFlow<Boolean>
        get() = _isDialog.asStateFlow()

    private val _smsTable = MutableStateFlow(SMSTable())
    val smsTable: StateFlow<SMSTable>
        get() = _smsTable.asStateFlow()

    private val _deleteUsersListStateFlow = MutableStateFlow(emptyList<User>())
    val deleteUsersListStateFlow
        get() = _deleteUsersListStateFlow.asStateFlow()

    private val _deleteMessagesListStateFlow = MutableStateFlow(emptyList<Message>())
    val deleteMessagesListStateFlow
        get() = _deleteMessagesListStateFlow.asStateFlow()

    init {
        instance = this

        viewModelScope.launch {
            context.contentResolver
                .register(Telephony.Sms.CONTENT_URI)
                .collect {
                    doCommand<Nothing>(
                        Commands.UPDATE_SMS,
                        context
                    )
                }
        }

        doCommand<Nothing>(
            Commands.UPDATE_SMS,
            context
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> doCommand(command: Commands, vararg data: Any? = arrayOf(null)): T? {
        when (command) {
            /*
            ROOM не используется

            Commands.GET_USERS -> return _dao.getUsers() as T
            Commands.GET_MESSAGES -> return _dao.getMessages(data as Int) as T
            Commands.GET_LAST_MESSAGE -> return _dao.getLastMessages() as T

            Commands.INSERT_USER -> {
                if (data is List<*>) {
                    viewModelScope.launch { _dao.insertUser(*(data as List<User>).toTypedArray()) }
                } else
                    viewModelScope.launch { _dao.insertUser(data as User) }
            }

            Commands.DELETE_USER -> {
                if (data is List<*>) {
                    viewModelScope.launch { _dao.deleteUser(*(data as List<User>).toTypedArray()) }
                } else {
                    viewModelScope.launch { _dao.deleteUser(data as User) }
                }
            }

            Commands.INSERT_MESSAGE -> if (data is List<*>)
                viewModelScope.launch { _dao.insertMessage(*(data as List<Message>).toTypedArray()) }
            else
                viewModelScope.launch { _dao.insertMessage(data as Message) }

            Commands.DELETE_MESSAGE -> viewModelScope.launch { _dao.deleteMessage(data as Message) }
            Commands.DELETE_USERS -> viewModelScope.launch { _dao.deleteAllUsers() }
            Commands.DELETE_MESSAGES -> viewModelScope.launch { _dao.deleteAllMessages() }*/

            Commands.SWITCH_DIALOG_ON -> _isDialog.value = true
            Commands.SWITCH_DIALOG_OFF -> _isDialog.value = false

            Commands.DELETE_LIST_USERS_UPDATE -> _deleteUsersListStateFlow.update { data[0] as List<User> }
            Commands.DELETE_LIST_USERS_PLUS -> _deleteUsersListStateFlow.value += data[0] as User
            Commands.DELETE_LIST_USERS_MINUS -> _deleteUsersListStateFlow.value -= data[0] as User
            Commands.DELETE_THREADS -> {
                for (user in _deleteUsersListStateFlow.value) {
                    Repository.deleteThreadFromId(data[0] as Context, user.id)
                }
                _deleteUsersListStateFlow.value = emptyList()
                //doCommand<Nothing>(Commands.UPDATE_SMS, data)
            }

            Commands.UPDATE_SMS -> {
                Log.i(TAG, "doCommand: sms updated")
                viewModelScope.launch {
                    _smsTable.value = Repository.refreshSmsInbox(data[0] as Context)
                }
            }

            Commands.SEND_SMS -> {
                data as Array<Any>
                Repository.sendSMS(
                    data[0] as Context, // Context
                    data[1] as String, // message
                    data[2] as String, // address
                    data[3] as Boolean // isDigits
                )
            }

            Commands.GET_CONTACTS -> return Repository.getContactList(data[0] as Context) as T
            Commands.ADD_USER -> _smsTable.value.users += data[0] as User

            Commands.INSERT_SMS -> {
                Repository.insertSMS(
                    data[0] as Context,
                    data[1] as Int,
                    data[2] as String,
                    data[3] as String
                )
            }


            Commands.DELETE_LIST_MESSAGES_UPDATE -> _deleteMessagesListStateFlow.value = data[0] as List<Message>
            Commands.DELETE_LIST_MESSAGES_PLUS -> _deleteMessagesListStateFlow.value += data[0] as Message
            Commands.DELETE_LIST_MESSAGES_MINUS -> _deleteMessagesListStateFlow.value += data[0] as Message
            Commands.DELETE_MESSAGES -> {

                Repository.deleteMessagesByIds(
                    data[0] as Context,
                    *(_deleteMessagesListStateFlow.value).map { m -> m.id }.toLongArray()
                )

                _deleteMessagesListStateFlow.value = emptyList()
            }

            Commands.SEND_NOTIFICATION -> {
                val context = data[0] as Context
                val address = data[1] as String
                val message = data[2] as String

                notificationManager.notify(
                    HiltModule.NOTIFICATION_ID,
                    NotificationCompat.Builder(context, HiltModule.CHANNEL_ID)
                        .setSmallIcon(R.drawable.baseline_message_24)
                        .setContentTitle(address)
                        .setContentText(message)
                        .setContentIntent(
                            PendingIntent.getActivity(
                                context,
                                11,
                                Intent(context, StartActivity::class.java),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                        .build()
                    )
            }
        }
        return null
    }

    fun vibrate() {
        vibrator.vibrate(VibrationEffect.createOneShot(50, 255))
    }
}
