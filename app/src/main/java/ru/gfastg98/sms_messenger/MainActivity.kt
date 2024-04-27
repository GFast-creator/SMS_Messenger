package ru.gfastg98.sms_messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.gfastg98.sms_messenger.ui.theme.SMSMessengerTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    enum class ROUTS(val r : String){
        USERS("users"), MESSAGES("messages");


        fun String.to(s: String): String{
            return "$this/$s"
        }
        fun ROUTS.to(s: String): String{
            return "${this.r}/$s"
        }
    }

    private val viewModel: MessengerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SMSMessengerTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(id = R.string.app_name)) },
                            actions = {
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription  = null)
                                }
                            }
                        ) },
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "users"
                    ) {
                        composable(ROUTS.USERS.r) { UsersScreen(viewModel, modifier) }
                        composable(ROUTS.MESSAGES.r + "/{userId}") {navBackStack ->

                            MessagesScreen(navBackStack.arguments?.getLong("userId"), modifier) }
                    }
                }
            }
        }
    }


    @Preview(
        showBackground = true,
        showSystemUi = true
    )
    @Composable
    fun ScreenPreview() {
        MessagesScreen(0, Modifier)
    }

    @Preview(
        showBackground = true,
        showSystemUi = true
    )
    @Composable
    fun MessageCardPreview() {
        Column {
            for (i in 1..10) {
                MessageCard(
                    message = Message(
                        text = "19\n18",
                        datetime = Date(),
                        userId = i,
                        fromId = if (i % 2 == 0) i else 0
                    )
                )
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
                .padding(horizontal = 8.dp),
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

    @Preview(
        showBackground = true,
        showSystemUi = true
    )
    @Composable
    fun UserCardPreview() {
        Column {
            UserCard(Modifier, user = User(), Message(text = "127059324750923745972359728570923758237459237592290345723049857209387592387582379520934578923573", datetime = Date(), userId = 0, fromId = 0))
            UserCard(Modifier, user = User(), Message(text = "123", datetime = Calendar.getInstance().apply { set(1989,11,1,1,1,3)}.time, userId = 0, fromId = 0))
            UserCard(Modifier, user = User(), Message(text = "123", datetime = Date(), userId = 0, fromId = 0))
            UserCard(Modifier, user = User(), Message(text = "123", datetime = Date(), userId = 0, fromId = 0))
            UserCard(Modifier, user = User(), Message(text = "123", datetime = Date(), userId = 0, fromId = 0))
        }
    }






}

fun Date.isToday() : Boolean{
    return date == Date().date &&
            month == Date().month &&
            year == Date().year
}