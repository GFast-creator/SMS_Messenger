package ru.gfastg98.sms_messenger.activites

import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ru.gfastg98.sms_messenger.AddDialog
import ru.gfastg98.sms_messenger.Commands
import ru.gfastg98.sms_messenger.Commands.DELETE_LIST_UPDATE
import ru.gfastg98.sms_messenger.Commands.SWITCH_DIALOG_ON
import ru.gfastg98.sms_messenger.Commands.UPDATE_SMS
import ru.gfastg98.sms_messenger.MessengerViewModel
import ru.gfastg98.sms_messenger.R
import ru.gfastg98.sms_messenger.room.Message
import ru.gfastg98.sms_messenger.room.User
import ru.gfastg98.sms_messenger.screens.MessageCard
import ru.gfastg98.sms_messenger.screens.MessagesScreen
import ru.gfastg98.sms_messenger.screens.UserCard
import ru.gfastg98.sms_messenger.screens.UsersScreen
import ru.gfastg98.sms_messenger.ui.theme.SMSMessengerTheme
import ru.gfastg98.sms_messenger.ui.theme.getInvertedColor
import java.util.Calendar
import java.util.Date

enum class ROUTS(val r: String) {
    USERS("users"), MESSAGES("messages")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MessengerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SMSMessengerTheme {
                val navController = rememberNavController()

                val appName = stringResource(id = R.string.app_name)
                var title by remember { mutableStateOf("") }
                var topAppBarColor by remember { mutableStateOf(Color.Unspecified) }
                var addButtonVisibility by remember{ mutableStateOf(true)}

                val isDialog by viewModel.isDialogStateFlow.collectAsState()
                val deleteList by viewModel.deleteUsersStateFlow.collectAsState()

                viewModel.doCommand<Nothing>(UPDATE_SMS, applicationContext) // обновление списка sms
                val messagesList by viewModel.smsTable.collectAsState()
                val contacts =
                    viewModel.doCommand<List<User>>(Commands.GET_CONTACTS, LocalContext.current)!!

                navController.addOnDestinationChangedListener { _, navDestination, bundle ->
                    if (navDestination.route?.contains(ROUTS.USERS.r) != false) {
                        title = appName
                        topAppBarColor = Color.Unspecified
                        addButtonVisibility = true
                    } else {
                        val user =
                            messagesList.users.firstOrNull { u -> u.id == bundle?.getLong("userId") }
                        title = user?.name ?: appName
                        topAppBarColor = user?.color ?: Color.Unspecified
                        addButtonVisibility = false
                    }
                }

                if (isDialog) {
                    AddDialog(viewModel, navController, contacts)
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = title) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = topAppBarColor,
                                titleContentColor = (getInvertedColor(topAppBarColor))
                            ),
                            actions = {
                                if (deleteList.isNotEmpty()) {
                                    IconButton(onClick = {
                                        viewModel.doCommand<Nothing>(
                                            DELETE_LIST_UPDATE,
                                            emptyList<User>()
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(id = R.string.cancel)
                                        )
                                    }

                                    IconButton(onClick = {
                                        viewModel.doCommand<Nothing>(
                                            DELETE_LIST_UPDATE,
                                            messagesList.users
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.SelectAll,
                                            contentDescription = stringResource(id = R.string.select_all),
                                        )
                                    }
                                    IconButton(onClick = {
                                        /*viewModel.doCommand<Nothing>(DELETE_USER, deleteList)
                                        viewModel.doCommand<Nothing>(
                                            DELETE_LIST_UPDATE,
                                            emptyList<User>()
                                        )*/
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_selected)
                                        )
                                    }
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        if (addButtonVisibility)
                            FloatingActionButton(
                                onClick = {
                                    //navController.navigate(ROUTS.MESSAGES.r + "/-1")
                                    viewModel.doCommand<Nothing>(SWITCH_DIALOG_ON)
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.new_message)
                                )
                            }
                    },
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    NavHost(
                        navController = navController,
                        startDestination = ROUTS.USERS.r
                    ) {
                        composable(ROUTS.USERS.r) {
                            UsersScreen(
                                navController = navController,
                                modifier = modifier,
                                viewModel = viewModel,
                                users = messagesList.users,
                                messages = messagesList.messages,
                                deleteList = deleteList
                            )
                        }
                        composable(
                            route = "${ROUTS.MESSAGES.r}/{userId}",
                            enterTransition = {
                                fadeIn(
                                    animationSpec = tween(
                                        300, easing = LinearEasing
                                    )
                                ) + slideIntoContainer(
                                    animationSpec = tween(300, easing = EaseIn),
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                                )
                            },
                            exitTransition = {
                                fadeOut(
                                    animationSpec = tween(
                                        300, easing = LinearEasing
                                    )
                                ) + slideOutOfContainer(
                                    animationSpec = tween(300, easing = EaseOut),
                                    towards = AnimatedContentTransitionScope.SlideDirection.End
                                )
                            },
                            arguments = listOf(
                                navArgument("userId") {
                                    type = NavType.LongType
                                }
                            )
                        ) { navBackStack ->
                            val userId = navBackStack.arguments?.getLong("userId")
                            val currentUser = messagesList.users.firstOrNull { u -> u.id == userId }

                            MessagesScreen(
                                messages = userId?.let {
                                    messagesList.messages.filter { m -> m.threadId == it.toInt() }
                                } ?: emptyList(),
                                userAddress = currentUser?.num,
                                modifier = modifier,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun MessageCardPreview() {
        Column {
            repeat(10) { index ->
                MessageCard(
                    message = Message(
                        text = "19\n18",
                        datetime = Date(),
                        threadId = 6,
                        type = if (index % 3 == 0) Telephony.Sms.MESSAGE_TYPE_OUTBOX
                        else Telephony.Sms.MESSAGE_TYPE_INBOX
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
                    threadId = 0,
                    type = Telephony.Sms.MESSAGE_TYPE_INBOX
                )
            )
            UserCard(
                user = User(),
                lastMessage = Message(
                    text = "123",
                    datetime = Calendar.getInstance().apply { set(1989, 11, 1, 1, 1, 3) }.time,
                    threadId = 0,
                    type = Telephony.Sms.MESSAGE_TYPE_INBOX
                ),
                selected = true
            )
            UserCard(
                user = User(),
                lastMessage = Message(
                    text = "123",
                    datetime = Date(),
                    threadId = 0,
                    type = Telephony.Sms.MESSAGE_TYPE_OUTBOX
                )
            )
            UserCard(
                user = User(),
                lastMessage = Message(
                    text = "123",
                    datetime = Date(),
                    threadId = 0,
                    type = Telephony.Sms.MESSAGE_TYPE_INBOX
                )
            )
            UserCard(
                user = User(),
                lastMessage = Message(
                    text = "123",
                    datetime = Date(),
                    threadId = 0,
                    type = Telephony.Sms.MESSAGE_TYPE_INBOX
                )
            )
        }
    }
}
