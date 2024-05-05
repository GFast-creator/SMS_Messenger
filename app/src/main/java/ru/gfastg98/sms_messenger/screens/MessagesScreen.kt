package ru.gfastg98.sms_messenger.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import ru.gfastg98.sms_messenger.Message
import ru.gfastg98.sms_messenger.MessengerViewModel
import java.util.Date

@Composable
fun MessagesScreen(
    viewModel: MessengerViewModel,
    userId: Int?,
    modifier: Modifier = Modifier
) {
    if (userId == null) {
        Text(text = "Пользователь не найден")
        return
    }

    Log.i("MessagesScreen", "MessagesScreen: id is $userId")

    val messages by viewModel
        .doCommand<Flow<List<Message>>>(MessengerViewModel.Commands.GET_MESSAGES, userId)!!
        .collectAsState(initial = emptyList())

    Log.i("MessagesScreen", "MessagesScreen: $messages")

    Column(modifier
        .background(MaterialTheme.colorScheme.background)){
        LazyColumn(
            modifier = Modifier
                .weight(1f)
        ) {
            items(messages.size) { index ->
                MessageCard(message = messages[index])
            }
        }
        Row {

            var message by remember { mutableStateOf("") }

            val onSendMessage : (message : String) -> Unit = {
                viewModel.doCommand<Nothing>(
                    MessengerViewModel.Commands.INSERT_MESSAGE,
                    Message(text = message, datetime = Date(), userId = userId)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = "Сообщение...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (message.isNotBlank()) {
                            onSendMessage(message)
                            message = ""
                        }
                    }),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (message.isNotBlank()) {
                                onSendMessage(message)
                                message = ""
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Send"
                            )
                        }
                    }
                )
            }
        }
    }


}

@Composable
fun MessageCard(
    modifier: Modifier = Modifier,
    message: Message
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = if (message.fromId == message.userId)
            Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Card(
            modifier,
            shape = RoundedCornerShape(
                topEnd = 16.dp,
                topStart = 16.dp,
                bottomStart = if (message.fromId == message.userId) 0.dp else 16.dp,
                bottomEnd = if (message.fromId == message.userId) 16.dp else 0.dp
            )
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp),
                text = message.text
            )
        }
    }
}