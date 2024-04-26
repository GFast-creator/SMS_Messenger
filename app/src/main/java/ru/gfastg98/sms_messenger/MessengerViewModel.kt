package ru.gfastg98.sms_messenger

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessengerViewModel @Inject constructor(private val _dao: MessageDao) : ViewModel() {

    private val TAG: String = MessengerViewModel::class.java.simpleName
    val users = _dao.getUsers()

    enum class Command{
        INSERT_USER, DELETE_USER,
        INSERT_MESSAGE, DELETE_MESSAGE,

        SWITCH_DIALOG_USER,SWITCH_DIALOG_MESSAGE,
    }

    fun doCommand (command: Command, data: Any) {
        when(command){
            else -> Log.e(TAG, "sendData: неизвестная команда", )
        }
    }
}