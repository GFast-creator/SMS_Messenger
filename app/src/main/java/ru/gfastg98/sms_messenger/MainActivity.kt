package ru.gfastg98.sms_messenger

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ru.gfastg98.sms_messenger.Commands.DELETE_LIST_UPDATE
import ru.gfastg98.sms_messenger.Commands.DELETE_MESSAGES
import ru.gfastg98.sms_messenger.Commands.DELETE_USER
import ru.gfastg98.sms_messenger.Commands.DELETE_USERS
import ru.gfastg98.sms_messenger.Commands.SWITCH_DIALOG_ON
import ru.gfastg98.sms_messenger.Commands.UPDATE_SMS
import ru.gfastg98.sms_messenger.screens.AddDialog
import ru.gfastg98.sms_messenger.screens.MessageCard
import ru.gfastg98.sms_messenger.screens.MessagesScreen
import ru.gfastg98.sms_messenger.screens.UserCard
import ru.gfastg98.sms_messenger.screens.UsersScreen
import ru.gfastg98.sms_messenger.ui.theme.SMSMessengerTheme
import ru.gfastg98.sms_messenger.ui.theme.colorPool
import java.util.Calendar
import java.util.Date

enum class ROUTS(val r: String) {
    USERS("users"), MESSAGES("messages")
}

fun ContentResolver.strQuery(parse: String): Cursor? =
    query(Uri.parse(parse), null, null, null)

public const val INBOX = "content://sms/inbox"
public const val SENT = "content://sms/sent"
public const val DRAFT = "content://sms/draft"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        var instance: MainActivity? = null
            private set
    }

    override fun onStart() {
        super.onStart()
        instance = this
    }

    private val viewModel: MessengerViewModel by viewModels()

    fun updateSms() {
        viewModel.doCommand<Nothing>(UPDATE_SMS, refreshSmsInbox())
    }

    private fun refreshSmsInbox(): SMSTable {

        viewModel.doCommand<Nothing>(DELETE_USERS)
        viewModel.doCommand<Nothing>(DELETE_MESSAGES)

        val users = mutableListOf<User>()
        val smsList = mutableListOf<Message>()
        val cursor =
            contentResolver.strQuery(INBOX)
                ?: return (emptyList<User>() to emptyList())

        val indexBody = cursor.getColumnIndex("body")
        val indexAddress = cursor.getColumnIndex("address")

        if (indexBody < 0 || !cursor.moveToFirst()) return (emptyList<User>() to emptyList())

        do {
            val username = cursor.getString(indexAddress)
            val message = cursor.getString(indexBody)
            val userIndex = users.indexOfFirst { u -> u.name == username }
            if (userIndex != -1) {
                smsList += Message(
                    text = message,
                    datetime = Date(),
                    userId = users[userIndex].id,
                    fromId = users[userIndex].id
                )
            } else {
                val newUser = User((users.lastOrNull()?.id?.plus(1)) ?: 0, name = username)
                newUser.color = colorPool[newUser.id % 7]
                users += newUser
                smsList += Message(
                    text = message,
                    datetime = Date(),
                    userId = newUser.id,
                    fromId = newUser.id
                )
            }
        } while (cursor.moveToNext())

        cursor.close()

        return users to smsList
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SMSMessengerTheme {
                val navController = rememberNavController()
                val isDialog by viewModel.isDialogStateFlow.collectAsState()
                val deleteList by viewModel.deleteUsersStateFlow.collectAsState()

                updateSms()
                val messagesList by viewModel.smsTable.collectAsState()

                /*val users by viewModel
                    .doCommand<UsersTable>(GET_USERS)!!
                    .collectAsState(initial = emptyList())

                val lastMessages by viewModel
                    .doCommand<MessagesTable>(GET_LAST_MESSAGE)!!
                    .collectAsState(initial = emptyList())*/

                if (isDialog) {
                    AddDialog(viewModel)
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(id = R.string.app_name)) },
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
                                        /*viewModel.doCommand<Nothing>(
                                            DELETE_LIST_UPDATE,
                                            users
                                        )*/
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.SelectAll,
                                            contentDescription = stringResource(id = R.string.select_all),
                                        )
                                    }
                                    IconButton(onClick = {
                                        viewModel.doCommand<Nothing>(DELETE_USER, deleteList)
                                        viewModel.doCommand<Nothing>(
                                            DELETE_LIST_UPDATE,
                                            emptyList<User>()
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_selected)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            viewModel.doCommand<Nothing>(SWITCH_DIALOG_ON)
                                        }) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {

                            }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Новое сообщение"
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
                                users = messagesList.first,
                                messages = messagesList.second,
                                deleteList = deleteList
                            )
                            /*UsersScreen(
                                navController = navController,
                                modifier = modifier,
                                viewModel = viewModel,
                                users = users,
                                lastMessages = lastMessages,
                                deleteList = deleteList
                            )*/
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
                                    type = NavType.IntType
                                }
                            )
                        ) { navBackStack ->
                            val userId = navBackStack.arguments?.getInt("userId")
                            val messages = messagesList.second.filter { it.userId == userId }
                            MessagesScreen(
                                messages = messages,
                                userId = userId,
                                modifier = modifier
                            )
                            /*MessagesScreen(
                                viewModel = viewModel,
                                userId = navBackStack.arguments?.getInt("userId"),
                                modifier = modifier
                            )*/
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
                ),
                selected = true
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