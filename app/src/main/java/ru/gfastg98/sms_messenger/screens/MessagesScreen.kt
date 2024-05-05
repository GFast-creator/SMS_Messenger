package ru.gfastg98.sms_messenger.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import ru.gfastg98.sms_messenger.Message
import ru.gfastg98.sms_messenger.MessengerViewModel

@Composable
fun MessagesScreen(
    viewModel: MessengerViewModel,
    userId: Long?,
    modifier: Modifier = Modifier
) {
    if (userId == null){
        Text(text = "Пользователь не найден")
        return
    }

    val messages by viewModel
        .doCommand<Flow<List<Message>>>(MessengerViewModel.Commands.GET_MESSAGES, userId)!!
        .collectAsState(initial = emptyList())

    LazyColumn(modifier = modifier){
        items(messages.size) {index->
            MessageCard(message = messages[index])
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
            .padding(horizontal = 8.dp, vertical = 8.dp),
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