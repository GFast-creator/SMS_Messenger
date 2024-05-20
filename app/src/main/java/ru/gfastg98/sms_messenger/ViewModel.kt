package ru.gfastg98.sms_messenger

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import kotlinx.coroutines.launch
import ru.gfastg98.sms_messenger.Command.ADD_USER
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_MESSAGES_MINUS
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_MESSAGES_PLUS
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_MESSAGES_UPDATE
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_USERS_MINUS
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_USERS_PLUS
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_USERS_UPDATE
import ru.gfastg98.sms_messenger.Command.DELETE_MESSAGES
import ru.gfastg98.sms_messenger.Command.DELETE_THREADS
import ru.gfastg98.sms_messenger.Command.EXPORT_SMS
import ru.gfastg98.sms_messenger.Command.GET_CONTACTS
import ru.gfastg98.sms_messenger.Command.IMPORT_SMS
import ru.gfastg98.sms_messenger.Command.INSERT_SMS
import ru.gfastg98.sms_messenger.Command.SEND_NOTIFICATION
import ru.gfastg98.sms_messenger.Command.SEND_SMS
import ru.gfastg98.sms_messenger.Command.SWITCH_DIALOG_OFF
import ru.gfastg98.sms_messenger.Command.SWITCH_DIALOG_ON
import ru.gfastg98.sms_messenger.Command.UPDATE_SMS
import ru.gfastg98.sms_messenger.activites.StartActivity
import ru.gfastg98.sms_messenger.room.Message
import ru.gfastg98.sms_messenger.room.User
import java.io.File
import javax.inject.Inject

sealed interface Command {
    //GET_USERS, GET_MESSAGES, GET_LAST_MESSAGE,

    //INSERT_USER, DELETE_USER, DELETE_USERS,
    //INSERT_MESSAGE, DELETE_MESSAGE, DELETE_MESSAGES,
    data object SWITCH_DIALOG_ON : Command
    data object SWITCH_DIALOG_OFF : Command

    data class DELETE_LIST_USERS_UPDATE(val users: List<User>) : Command
    data class DELETE_LIST_USERS_PLUS(val user: User) : Command
    data class DELETE_LIST_USERS_MINUS(val user: User) : Command
    data class DELETE_LIST_MESSAGES_UPDATE(val messages: List<Message>) : Command
    data class DELETE_LIST_MESSAGES_PLUS(val message: Message) : Command
    data class DELETE_LIST_MESSAGES_MINUS(val message: Message) : Command

    data class UPDATE_SMS(val context: Context) : Command

    data class SEND_SMS(
        val context: Context,
        val address: String,
        val isDigits: Boolean,
        val message: String
    ) : Command

    data class ADD_USER(val user: User) : Command

    data class GET_CONTACTS(val context: Context) : Command

    data class INSERT_SMS(
        val context: Context,
        val type: Int,
        val address: String,
        val message: String
    ) : Command

    data class DELETE_THREADS(val context: Context) : Command
    data class DELETE_MESSAGES(val context: Context) : Command

    data class SEND_NOTIFICATION(
        val context: Context,
        val address: String,
        val message: String
    ) : Command

    data class IMPORT_SMS(val context: Context, val fileUri: Uri) : Command
    data class EXPORT_SMS(val context: Context, val directory: File) : Command
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

    @Inject
    lateinit var vibrator: Vibrator

    @Inject
    lateinit var notificationManager: NotificationManager

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
                    onEvent<Unit>(UPDATE_SMS(context))
                }
        }

        onEvent<Unit>(
            UPDATE_SMS(context = context)
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> onEvent(event: Command): T {
        when (event) {
            /*
            ROOM не используется

            Command.GET_USERS -> return _dao.getUsers() as T
            Command.GET_MESSAGES -> return _dao.getMessages(data as Int) as T
            Command.GET_LAST_MESSAGE -> return _dao.getLastMessages() as T

            Command.INSERT_USER -> {
                if (data is List<*>) {
                    viewModelScope.launch { _dao.insertUser(*(data as List<User>).toTypedArray()) }
                } else
                    viewModelScope.launch { _dao.insertUser(data as User) }
            }

            Command.DELETE_USER -> {
                if (data is List<*>) {
                    viewModelScope.launch { _dao.deleteUser(*(data as List<User>).toTypedArray()) }
                } else {
                    viewModelScope.launch { _dao.deleteUser(data as User) }
                }
            }

            Command.INSERT_MESSAGE -> if (data is List<*>)
                viewModelScope.launch { _dao.insertMessage(*(data as List<Message>).toTypedArray()) }
            else
                viewModelScope.launch { _dao.insertMessage(data as Message) }

            Command.DELETE_MESSAGE -> viewModelScope.launch { _dao.deleteMessage(data as Message) }
            Command.DELETE_USERS -> viewModelScope.launch { _dao.deleteAllUsers() }
            Command.DELETE_MESSAGES -> viewModelScope.launch { _dao.deleteAllMessages() }*/

            is SWITCH_DIALOG_ON -> _isDialog.value = true
            SWITCH_DIALOG_OFF -> _isDialog.value = false

            is DELETE_LIST_USERS_UPDATE -> _deleteUsersListStateFlow.value = event.users
            is DELETE_LIST_USERS_PLUS -> _deleteUsersListStateFlow.value += event.user
            is DELETE_LIST_USERS_MINUS -> _deleteUsersListStateFlow.value -= event.user
            is DELETE_THREADS -> {
                for (user in _deleteUsersListStateFlow.value) {
                    Repository.deleteThreadFromId(event.context, user.id)
                }
                _deleteUsersListStateFlow.value = emptyList()
                //doCommand<Nothing>(UPDATE_SMS, data)
            }

            is UPDATE_SMS -> {
                Log.i(TAG, "doCommand: sms updated")
                viewModelScope.launch {
                    _smsTable.value = Repository.refreshSmsInbox(event.context)
                }
            }

            is SEND_SMS -> Repository.sendSMS(
                event.context,
                event.address,
                event.isDigits,
                event.message
            )

            is GET_CONTACTS -> return Repository.getContactList(event.context) as T
            is ADD_USER -> _smsTable.value.users += event.user

            is INSERT_SMS -> {
                Repository.insertSMS(
                    event.context,
                    event.type,
                    event.address,
                    event.message,
                )
            }


            is DELETE_LIST_MESSAGES_UPDATE -> _deleteMessagesListStateFlow.value = event.messages
            is DELETE_LIST_MESSAGES_PLUS -> _deleteMessagesListStateFlow.value += event.message
            is DELETE_LIST_MESSAGES_MINUS -> _deleteMessagesListStateFlow.value += event.message
            is DELETE_MESSAGES -> {

                Repository.deleteMessagesByIds(
                    event.context,
                    *(_deleteMessagesListStateFlow.value).map { m -> m.id }.toLongArray()
                )

                _deleteMessagesListStateFlow.value = emptyList()
            }

            is SEND_NOTIFICATION -> {
                notificationManager.notify(
                    HiltModule.NOTIFICATION_ID,
                    NotificationCompat.Builder(event.context, HiltModule.CHANNEL_ID)
                        .setSmallIcon(R.drawable.baseline_message_24)
                        .setContentTitle(event.address)
                        .setContentText(event.message)
                        .setContentIntent(
                            PendingIntent.getActivity(
                                event.context,
                                11,
                                Intent(event.context, StartActivity::class.java),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                        .build()
                )
            }

            is IMPORT_SMS -> return Repository.import(
                context = event.context,
                destination = event.fileUri
            ) as T


            is EXPORT_SMS -> {
                return Repository.export(
                    context = event.context,
                    destination = event.directory,
                    table = _smsTable.value
                ) as T
            }

        }
        return Unit as T
    }

    fun vibrate() {
        vibrator.vibrate(VibrationEffect.createOneShot(50, 255))
    }
}
