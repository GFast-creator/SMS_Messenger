package ru.gfastg98.sms_messenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class MessengerViewModel @Inject constructor(private val _dao: MessageDao) : ViewModel() {

    private val TAG: String = MessengerViewModel::class.java.simpleName

    enum class Commands{
        GET_USERS, GET_MESSAGES, GET_LAST_MESSAGE,

        INSERT_USER, DELETE_USER,
        INSERT_MESSAGE, DELETE_MESSAGE,

        SWITCH_DIALOG_ON,SWITCH_DIALOG_OFF
    }

    val users
        get() = _dao.getUsers()

    private val _isDialog = MutableStateFlow(false)

    val isDialogStateFlow: StateFlow<Boolean>
        get() = _isDialog.asStateFlow()

    fun update(newState : Boolean){
        _isDialog.value = newState
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> doCommand (command: Commands, data: Any? = null) : T? {
        when(command){
            Commands.GET_USERS -> return _dao.getUsers() as T
            Commands.GET_MESSAGES -> return _dao.getMessages(data as Int) as T
            Commands.GET_LAST_MESSAGE -> return _dao.getLastMessages() as T

            Commands.INSERT_USER -> viewModelScope.launch { _dao.insertUser(data as User)}
            Commands.DELETE_USER -> viewModelScope.launch { _dao.deleteUser(data as User)}

            Commands.INSERT_MESSAGE -> viewModelScope.launch { _dao.insertMessage(data as Message)}
            Commands.DELETE_MESSAGE -> viewModelScope.launch { _dao.deleteMessage(data as Message)}

            Commands.SWITCH_DIALOG_ON -> _isDialog.value = true
            Commands.SWITCH_DIALOG_OFF -> _isDialog.value = false
        }
        return null
    }
}
