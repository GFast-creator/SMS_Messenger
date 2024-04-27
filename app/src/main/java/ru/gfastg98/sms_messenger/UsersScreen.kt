package ru.gfastg98.sms_messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UsersScreen(
    viewModel: MessengerViewModel,
    modifier: Modifier
) {

    val users by viewModel.users.collectAsState(initial = emptyList())


    LazyColumn(modifier) {
        items(users.size) {index ->
            UserCard(modifier = Modifier, user = users[index], lastMessage = )
        }
    }
}

@Composable
fun UserCard(
    modifier: Modifier,
    user: User,
    lastMessage: Message
) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .fillMaxWidth()
            .padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .background(user.color)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = user.name.run {
                    val x = uppercase().split(" ")
                    x.joinToString(separator = "") { s -> s[0].toString() }
                })
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Text(
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    text = /*if (DateUtils.isToday(lastMessage.datetime.time))
                            SimpleDateFormat("h:mm", Locale.ROOT).format(lastMessage.datetime)
                        else
                            SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).format(lastMessage.datetime)*/
                    if (lastMessage.datetime.isToday())
                        SimpleDateFormat("HH:mm", Locale.ROOT).format(lastMessage.datetime)
                    else SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).format(lastMessage.datetime)

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

