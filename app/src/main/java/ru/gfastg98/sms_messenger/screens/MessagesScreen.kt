package ru.gfastg98.sms_messenger.screens

import android.provider.Telephony
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_MESSAGES_MINUS
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_MESSAGES_PLUS
import ru.gfastg98.sms_messenger.Command.INSERT_SMS
import ru.gfastg98.sms_messenger.MessengerViewModel
import ru.gfastg98.sms_messenger.R
import ru.gfastg98.sms_messenger.activites.MainActivity
import ru.gfastg98.sms_messenger.messageTypeIcon
import ru.gfastg98.sms_messenger.room.Message
import ru.gfastg98.sms_messenger.ui.theme.ItemColorRed
import ru.gfastg98.sms_messenger.ui.theme.checkColor
import ru.gfastg98.sms_messenger.ui.theme.shapes.SpeechBubbleShape
import java.util.Date


@Composable
fun MessagesScreen(
    viewModel: MessengerViewModel,
    messages: List<Message>,
    userAddress: String?,
    modifier: Modifier,
    navController: NavController
) {

    BackHandler {
        if (MainActivity.onBackPressedCallback.isEnabled){
            MainActivity.onBackPressedCallback.handleOnBackPressed()
        } else navController.popBackStack()
    }

    val deleteListMessages by viewModel.deleteMessagesListStateFlow.collectAsState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (userAddress == null) {
            Text(text = stringResource(R.string.error_user_is_not_found))
            return
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            reverseLayout = true
        ) {
            items(messages) { message ->
                MessageCard(
                    message = message,
                    onItemClick = {
                        if (deleteListMessages.isNotEmpty()){
                            if (message in deleteListMessages){
                                viewModel.onEvent<Unit>(
                                    DELETE_LIST_MESSAGES_MINUS(message)
                                )
                            } else {
                                viewModel.onEvent<Unit>(
                                    DELETE_LIST_MESSAGES_PLUS(message)
                                )
                            }
                        } else MainActivity.onBackPressedCallback.isEnabled = false
                    },
                    onItemLongClick = {
                        if (deleteListMessages.isEmpty()){
                            viewModel.onEvent<Unit>(
                                DELETE_LIST_MESSAGES_PLUS(message)
                            )
                            MainActivity.onBackPressedCallback.isEnabled = true
                        }
                    },
                    isSelected = message in deleteListMessages
                )
            }
        }
        Row {
            var message by remember { mutableStateOf("") }
            val context = LocalContext.current
            val onSendMessage: (message: String) -> Unit = {
                viewModel.onEvent<Unit>(
                    INSERT_SMS(
                        context = context,
                        type = Telephony.Sms.MESSAGE_TYPE_SENT,
                        address = userAddress,
                        message = message.trim()
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = stringResource(R.string.label_message_line)) },
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
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.send)
                            )
                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageCard(
    modifier: Modifier = Modifier,
    message: Message,
    isSelected: Boolean = false,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            )
            .run {
                if (isSelected)
                    background(ItemColorRed)
                else this
            },
        contentAlignment = if (message.type == Telephony.Sms.MESSAGE_TYPE_INBOX)
            Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Box(
            Modifier
                .clip(
                    /*RoundedCornerShape(
                        topEnd = 16.dp,
                        topStart = 16.dp,
                        bottomStart = if (message.type == Telephony.Sms.MESSAGE_TYPE_INBOX) 0.dp else 16.dp,
                        bottomEnd = if (message.type == Telephony.Sms.MESSAGE_TYPE_INBOX) 16.dp else 0.dp
                    )*/
                    SpeechBubbleShape(isInboxMessage = message.type == Telephony.Sms.MESSAGE_TYPE_INBOX)
                )
        ) {
            Box(
                modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                    .run {
                        if (message.type == Telephony.Sms.MESSAGE_TYPE_INBOX)
                            padding(start = 12.dp)
                        else
                            padding(end = 12.dp)
                    }
            ) {
                Text(
                    modifier = Modifier
                        .padding(16.dp),
                    text = message.text
                )
                if (message.type != Telephony.Sms.MESSAGE_TYPE_INBOX) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .padding(end = 4.dp, bottom = 2.dp),

                        imageVector = message.type.messageTypeIcon,
                        contentDescription = null,
                        tint = if (message.type == Telephony.Sms.MESSAGE_TYPE_FAILED) ItemColorRed
                        else checkColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MessageCardPreview() {
    MessageCard(
        message = Message(
            text = "TEST",
            threadId = 0,
            datetime = Date(),
            type = Telephony.Sms.MESSAGE_TYPE_SENT
        ),
        isSelected = true,
        onItemClick = {},
        onItemLongClick = {}
    )
}