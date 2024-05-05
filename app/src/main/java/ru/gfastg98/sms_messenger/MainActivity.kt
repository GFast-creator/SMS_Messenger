package ru.gfastg98.sms_messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.gfastg98.sms_messenger.screens.AddDialog
import ru.gfastg98.sms_messenger.screens.MessageCard
import ru.gfastg98.sms_messenger.screens.MessagesScreen
import ru.gfastg98.sms_messenger.screens.UserCard
import ru.gfastg98.sms_messenger.screens.UsersScreen
import ru.gfastg98.sms_messenger.ui.theme.SMSMessengerTheme
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    enum class ROUTS(val r: String) {
        USERS("users"), MESSAGES("messages")
    }

    private val viewModel: MessengerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SMSMessengerTheme {
                val navController = rememberNavController()
                val isDialog by viewModel.isDialogStateFlow.collectAsState()

                if (isDialog){
                    AddDialog(viewModel)
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(id = R.string.app_name)) },
                            actions = {
                                IconButton(
                                    onClick = {
                                        viewModel.doCommand<Nothing>(MessengerViewModel.Commands.SWITCH_DIALOG_ON)
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->

                    val modifier = Modifier.padding(innerPadding)
                    
                    NavHost(
                        navController = navController,
                        startDestination = ROUTS.USERS.r
                    ) {
                        composable(ROUTS.USERS.r) { UsersScreen(viewModel, modifier) }
                        composable("${ROUTS.MESSAGES.r}/{userId}") { navBackStack ->
                            MessagesScreen(
                                viewModel,
                                navBackStack.arguments?.getLong("userId"),
                                modifier
                            )
                        }
                    }
                }
            }
        }
    }


    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun ScreenPreview() {
        MessagesScreen(viewModel, 0, Modifier)
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun MessageCardPreview() {
        Column {
            for (i in 1..10) {
                MessageCard(
                    message = Message(
                        text = "19\n18",
                        datetime = Date(),
                        userId = i,
                        fromId = if (i % 3 == 0) i else 0
                    )
                )
            }
        }
    }


    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun UserCardPreview() {
        Column {
            UserCard(
                user = User(),
                lastMessage = Message(
                    text = "127059324750923745972359728570923758237459237592290345723049857209387592387582379520934578923573",
                    datetime = Date(),
                    userId = 0,
                    fromId = 0
                )
            )
            UserCard(
                user = User(),
                lastMessage = Message(
                    text = "123",
                    datetime = Calendar.getInstance().apply { set(1989, 11, 1, 1, 1, 3) }.time,
                    userId = 0,
                    fromId = 0
                )
            )
            UserCard(
                user = User(),
                lastMessage = Message(text = "123", datetime = Date(), userId = 0, fromId = 0)
            )
            UserCard(
                user = User(),
                lastMessage = Message(text = "123", datetime = Date(), userId = 0, fromId = 0)
            )
            UserCard(
                user = User(),
                lastMessage = Message(text = "123", datetime = Date(), userId = 0, fromId = 0)
            )
        }
    }
}

fun Date.isToday(): Boolean {
    return date == Date().date &&
            month == Date().month &&
            year == Date().year
}