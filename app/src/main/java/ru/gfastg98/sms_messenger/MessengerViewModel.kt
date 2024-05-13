package ru.gfastg98.sms_messenger

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.gfastg98.sms_messenger.room.Message
import ru.gfastg98.sms_messenger.room.MessageDao
import ru.gfastg98.sms_messenger.room.User
import javax.inject.Inject

enum class Commands {
    //GET_USERS, GET_MESSAGES, GET_LAST_MESSAGE,

    //INSERT_USER, DELETE_USER, DELETE_USERS,
    //INSERT_MESSAGE, DELETE_MESSAGE, DELETE_MESSAGES,

    SWITCH_DIALOG_ON, SWITCH_DIALOG_OFF,

    DELETE_LIST_UPDATE, DELETE_LIST_PLUS, DELETE_LIST_MINUS,

    UPDATE_SMS, SEND_SMS, ADD_USER, GET_CONTACTS,

    DELETE_THREAD, DELETE_MESSAGES
}

data class SMSTable(
    var users : List<User> = emptyList(),
    var messages: List<Message> = emptyList(),
)

@HiltViewModel
class MessengerViewModel @Inject constructor(private val _dao: MessageDao) : ViewModel() {
    companion object{
        const val TAG = "MessengerViewModel"
        var instance: MessengerViewModel? = null
    }
    init {
        instance = this
    }

    @Inject
    lateinit var vibrator: Vibrator

    private val _isDialog = MutableStateFlow(false)
    val isDialogStateFlow: StateFlow<Boolean>
        get() = _isDialog.asStateFlow()

    private val _smsTable = MutableStateFlow(SMSTable())
    val smsTable: StateFlow<SMSTable>
        get() = _smsTable.asStateFlow()

    private val _deleteUsersStateFlow = MutableStateFlow(emptyList<User>())
    val deleteUsersStateFlow
        get() = _deleteUsersStateFlow.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    fun <T> doCommand(command: Commands, data: Any? = null): T? {
        when (command) {
            //Commands.GET_USERS -> return _dao.getUsers() as T
            //Commands.GET_MESSAGES -> return _dao.getMessages(data as Int) as T
            //Commands.GET_LAST_MESSAGE -> return _dao.getLastMessages() as T

            /*Commands.INSERT_USER -> {
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

            Commands.DELETE_MESSAGE -> viewModelScope.launch { _dao.deleteMessage(data as Message) }*/

            Commands.SWITCH_DIALOG_ON -> _isDialog.value = true
            Commands.SWITCH_DIALOG_OFF -> _isDialog.value = false

            Commands.DELETE_LIST_UPDATE -> _deleteUsersStateFlow.update { data as List<User> }
            Commands.DELETE_LIST_PLUS -> _deleteUsersStateFlow.value += data as User
            Commands.DELETE_LIST_MINUS -> _deleteUsersStateFlow.value -= data as User

            //Commands.DELETE_USERS -> viewModelScope.launch { _dao.deleteAllUsers() }
            //Commands.DELETE_MESSAGES -> viewModelScope.launch { _dao.deleteAllMessages() }
            Commands.UPDATE_SMS -> _smsTable.value = Repository.refreshSmsInbox(data as Context)
            Commands.SEND_SMS -> {
                data as Array<Any>
                Repository.sendSMS(data[0] as Context, data[1] as String, data[2] as String, data[3] as Boolean)
            }

            Commands.GET_CONTACTS -> return Repository.getContactList(data as Context) as T
            Commands.ADD_USER -> _smsTable.value.users += data as User
            Commands.DELETE_THREAD -> {
                data as Array<*>
                Repository.deleteThreadFromId(data[0] as Context, data[1] as Long)
            }
            Commands.DELETE_MESSAGES -> {
                TODO()
            }
        }
        return null
    }

    fun vibrate() {
        vibrator.vibrate(VibrationEffect.createOneShot(50, 255))
    }
}
