package ru.gfastg98.sms_messenger.activites

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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
import androidx.compose.material.icons.filled.Archive
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ru.gfastg98.sms_messenger.Command
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_MESSAGES_UPDATE
import ru.gfastg98.sms_messenger.Command.DELETE_LIST_USERS_UPDATE
import ru.gfastg98.sms_messenger.Command.GET_CONTACTS
import ru.gfastg98.sms_messenger.Command.SWITCH_DIALOG_ON
import ru.gfastg98.sms_messenger.MessengerViewModel
import ru.gfastg98.sms_messenger.R
import ru.gfastg98.sms_messenger.dialogs.AddDialog
import ru.gfastg98.sms_messenger.dialogs.BackupDialog
import ru.gfastg98.sms_messenger.dialogs.DeleteDialog
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

    companion object {
        lateinit var onBackPressedCallback: OnBackPressedCallback
    }

    private val viewModel: MessengerViewModel by viewModels()
    private var currentUserId: Long? = null
    private var contacts: List<User> = emptyList()


    override fun onResume() {
        super.onResume()
        contacts = viewModel.onEvent(GET_CONTACTS(applicationContext))

        //проверка на приложение по умолчанию
        if (!Telephony.Sms.getDefaultSmsPackage(applicationContext).equals(packageName)) {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onEvent<Unit>(
                    DELETE_LIST_USERS_UPDATE(
                        emptyList()
                    )
                )
                viewModel.onEvent<Unit>(
                    DELETE_LIST_MESSAGES_UPDATE(
                        emptyList()
                    )
                )
                isEnabled = false
            }
        }

        setContent {
            SMSMessengerTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                val defColor = MaterialTheme.colorScheme.surface

                val navController = rememberNavController()

                val appName = stringResource(id = R.string.app_name)
                var title by remember { mutableStateOf("") }
                var topAppBarColor by remember { mutableStateOf(defColor) }
                var addButtonVisibility by remember { mutableStateOf(true) }

                val isDialog by viewModel.isDialogStateFlow.collectAsState()
                var isDeleteDialog by remember { mutableStateOf(false) }
                var isBackupDialog by remember { mutableStateOf(false) }

                val deleteListUsers by viewModel.deleteUsersListStateFlow.collectAsState()
                val deleteListMessages by viewModel.deleteMessagesListStateFlow.collectAsState()

                // обновление списка sms
                val messagesList by viewModel.smsTable.collectAsState()

                LaunchedEffect(null) {
                    onBackPressedDispatcher.addCallback(
                        lifecycleOwner,
                        onBackPressedCallback
                    )
                    navController.addOnDestinationChangedListener { _, navDestination, bundle ->
                        if (navDestination.route?.contains(ROUTS.USERS.r) == true) {
                            title = appName
                            topAppBarColor = Color.Unspecified
                            addButtonVisibility = true
                            currentUserId = null
                            viewModel.onEvent<Unit>(
                                DELETE_LIST_MESSAGES_UPDATE(emptyList())
                            )
                        } else {
                            onBackPressedCallback.isEnabled = false
                            currentUserId = bundle?.getLong("userId")
                            val user = messagesList.users.firstOrNull { u -> u.id == currentUserId }
                            title = user?.name ?: appName
                            topAppBarColor = user?.color ?: defColor
                            addButtonVisibility = false
                            viewModel.onEvent<Unit>(
                                DELETE_LIST_USERS_UPDATE(emptyList())
                            )
                        }
                    }
                }

                if (isDialog) {
                    AddDialog(viewModel, contacts)
                }

                if (isDeleteDialog) {
                    DeleteDialog(
                        onDismissAction = {
                            isDeleteDialog = false
                        },
                        onConfirmAction = {
                            viewModel.onEvent<Unit>(
                                if (deleteListUsers.isNotEmpty()) Command.DELETE_THREADS(
                                    applicationContext
                                )
                                else Command.DELETE_MESSAGES(applicationContext)
                            )
                            onBackPressedCallback.isEnabled = false
                            isDeleteDialog = false
                        }
                    )
                }

                if (isBackupDialog) {
                    BackupDialog(
                        viewModel = viewModel,
                        onDismissAction = { isBackupDialog = false },
                        startDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    )
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
                                if (deleteListUsers.isNotEmpty() || deleteListMessages.isNotEmpty()) {
                                    IconButton(onClick = {
                                        viewModel.onEvent<Unit>(
                                            if (deleteListUsers.isNotEmpty())
                                                DELETE_LIST_USERS_UPDATE(emptyList())
                                            else DELETE_LIST_MESSAGES_UPDATE(emptyList())
                                        )
                                        onBackPressedCallback.isEnabled = false
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(id = R.string.cancel)
                                        )
                                    }

                                    IconButton(onClick = {

                                        if (deleteListUsers.isNotEmpty())
                                            viewModel.onEvent<Unit>(
                                                DELETE_LIST_USERS_UPDATE(
                                                    messagesList.users
                                                )
                                            )
                                        else currentUserId?.let {
                                            viewModel.onEvent<Unit>(
                                                DELETE_LIST_MESSAGES_UPDATE(
                                                    messagesList.messages.filter { m ->
                                                        m.threadId == it
                                                    }
                                                )
                                            )
                                        }

                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.SelectAll,
                                            contentDescription = stringResource(id = R.string.select_all),
                                        )
                                    }

                                    IconButton(onClick = {
                                        isDeleteDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_selected)
                                        )
                                    }
                                } else if (currentUserId == null) {
                                    IconButton(onClick = {
                                        isBackupDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Archive,
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
                                    viewModel.onEvent<Unit>(SWITCH_DIALOG_ON)
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
                                deleteList = deleteListUsers
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
                                    messagesList.messages.filter { m -> m.threadId == it }
                                } ?: emptyList(),
                                userAddress = currentUser?.num,
                                modifier = modifier,
                                viewModel = viewModel,
                                navController = navController
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
                    ),
                    onItemLongClick = {},
                    onItemClick = {}
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
