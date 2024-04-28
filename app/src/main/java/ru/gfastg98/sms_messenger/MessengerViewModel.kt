package ru.gfastg98.sms_messenger

import android.util.Log
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessengerViewModel @Inject constructor(private val _dao: MessageDao) : ViewModel() {

    private val TAG: String = MessengerViewModel::class.java.simpleName

    enum class Command{
        GET_USERS, GET_MESSAGES, GET_LAST_MESSAGE,

        INSERT_USER, DELETE_USER,
        INSERT_MESSAGE, DELETE_MESSAGE,

        SWITCH_DIALOG_ON,SWITCH_DIALOG_OFF
    }

    private val _isDialog = MutableStateFlow(false)
    val isDialogStateFlow: StateFlow<Boolean>
        get() = _isDialog.asStateFlow()

    fun update(newState : Boolean){
        _isDialog.value = newState
    }

    /*object Instance{
        lateinit var users:Flow<List<User>>
    }

    init {
        Instance.users = doCommand(Command.GET_USERS)
    }*/

    @Suppress("UNCHECKED_CAST")
    fun <T> doCommand (command: Command, data: Any? = null) : T? {
        when(command){
            Command.GET_USERS -> return _dao.getUsers() as T
            Command.GET_MESSAGES -> return _dao.getMessages(data as Int) as T
            Command.GET_LAST_MESSAGE -> return _dao.getLastMessages() as T

            Command.INSERT_USER -> viewModelScope.launch { _dao.insertUser(data as User)}
            Command.DELETE_USER -> viewModelScope.launch { _dao.deleteUser(data as User)}

            Command.INSERT_MESSAGE -> viewModelScope.launch { _dao.insertMessage(data as Message)}
            Command.DELETE_MESSAGE -> viewModelScope.launch { _dao.deleteMessage(data as Message)}

            Command.SWITCH_DIALOG_ON -> _isDialog.value = true
            Command.SWITCH_DIALOG_OFF -> _isDialog.value = false

        }

        return null
    }
}