package ru.gfastg98.sms_messenger.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import ru.gfastg98.sms_messenger.Commands.DELETE_LIST_MINUS
import ru.gfastg98.sms_messenger.Commands.DELETE_LIST_PLUS
import ru.gfastg98.sms_messenger.MessengerViewModel
import ru.gfastg98.sms_messenger.activites.ROUTS
import ru.gfastg98.sms_messenger.isToday
import ru.gfastg98.sms_messenger.room.Message
import ru.gfastg98.sms_messenger.room.User
import ru.gfastg98.sms_messenger.ui.theme.ItemColorRed
import ru.gfastg98.sms_messenger.ui.theme.getInvertedColor
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsersScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: MessengerViewModel,
    users: List<User>,
    messages: List<Message> = emptyList(),
    deleteList: List<User>
) {
    LazyColumn(
        modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users.size, key = { it }) { index ->
            UserCard(
                modifier = Modifier.combinedClickable(
                    onClick = {
                        if (deleteList.isNotEmpty()) {
                            if (users[index] !in deleteList) {
                                viewModel.doCommand<Nothing>(
                                    DELETE_LIST_PLUS,
                                    users[index]
                                )
                            } else {
                                viewModel.doCommand<Nothing>(
                                    DELETE_LIST_MINUS,
                                    users[index]
                                )
                            }
                            viewModel.vibrate()
                        } else {
                            navController.navigate("${ROUTS.MESSAGES.r}/${users[index].id}")
                        }
                    },
                    onLongClick = {
                        if (deleteList.isEmpty()) {
                            viewModel.doCommand<Nothing>(
                                DELETE_LIST_PLUS,
                                users[index]
                            )
                            viewModel.vibrate()
                        }
                    }
                ),
                user = users[index],
                lastMessage = messages.find { m -> m.threadId == users[index].id.toInt() },
                selected = users[index] in deleteList
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserCardPrev() {
    UserCard(user = User(), selected = false)
}

@Composable
fun UserCard(
    modifier: Modifier = Modifier,
    user: User,
    lastMessage: Message? = null,
    selected: Boolean = false
) {
    Box(Modifier.run {
        if (selected)
            background(
                ItemColorRed,
                shape = RoundedCornerShape(4.dp)
            )
        else this
    }) {
        Row(
            modifier = modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(user.color)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.run {
                        if (isEmpty()) ""
                        else uppercase()
                            .split(" ")
                            .joinToString(separator = "") { s ->
                                s[0].toString()
                            }
                        },
                        color = getInvertedColor(color = user.color)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                val FORMAT_DATE = "dd.MM.yyyy"
                val FORMAT_TIME = "HH:mm"

                if (lastMessage != null)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            text =
                            if (lastMessage.datetime.isToday)
                                SimpleDateFormat("HH:mm", Locale.ROOT)
                                    .format(lastMessage.datetime)
                            else SimpleDateFormat(
                                "dd.MM.yyyy",
                                Locale.ROOT
                            ).format(lastMessage.datetime)

                        )
                        Text(
                            text = lastMessage.text,
                            color = Color.Gray,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2
                        )
                    }
            }
        }
    }
}

