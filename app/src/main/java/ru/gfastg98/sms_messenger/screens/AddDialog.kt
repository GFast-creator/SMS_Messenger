package ru.gfastg98.sms_messenger.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.gfastg98.sms_messenger.Commands.GET_USERS
import ru.gfastg98.sms_messenger.Commands.INSERT_USER
import ru.gfastg98.sms_messenger.Commands.SWITCH_DIALOG_OFF
import ru.gfastg98.sms_messenger.MessengerViewModel
import ru.gfastg98.sms_messenger.R
import ru.gfastg98.sms_messenger.User
import ru.gfastg98.sms_messenger.UsersTable
import ru.gfastg98.sms_messenger.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    viewModel: MessengerViewModel = viewModel()
) {
    val currentUsers by viewModel
        .doCommand<UsersTable>(GET_USERS)!!
        .collectAsState(initial = emptyList())
    var decision by remember {
        mutableStateOf(true)
    }
    var action: (() -> Boolean) = { true }

    var hasTriedToDismiss by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = {
            viewModel.doCommand<Nothing>(SWITCH_DIALOG_OFF)
        },
        title = { Text(text = stringResource(R.string.add)) },
        text = {
            Column {
                Row {
                    Row(Modifier
                        .clickable {
                            decision = true
                            hasTriedToDismiss = false
                        },
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.user))
                        RadioButton(selected = decision,
                            onClick = {
                                decision = true
                                hasTriedToDismiss = false
                            })
                    }
                    Row(
                        Modifier.clickable {
                            decision = false
                            hasTriedToDismiss = false
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.message))
                        RadioButton(
                            selected = !decision,
                            onClick = {
                                decision = false
                                hasTriedToDismiss = false
                            }
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (decision) {
                        //for user
                        var username by rememberSaveable { mutableStateOf("") }
                        var color by rememberSaveable { mutableStateOf("") }

                        var isUsernameError by remember { mutableStateOf(false) }
                        var isColorError by remember { mutableStateOf(false) }

                        if (hasTriedToDismiss && isUsernameError)
                            ErrorText(stringResource(R.string.error_empty_username))
                        TextField(
                            label = { Text(stringResource(R.string.username)) },
                            value = username, onValueChange = {
                                username = it
                                isUsernameError = username.isBlank()
                            })
                        if (hasTriedToDismiss && isColorError)
                            ErrorText(stringResource(R.string.error_color))
                        TextField(
                            label = { Text(stringResource(R.string.title_color)) },
                            value = color,
                            onValueChange = {
                                color = it
                                isColorError = checkColor(color)
                            },
                            colors = TextFieldDefaults.run {

                                if (!checkColor(color)) {
                                    colors(
                                        focusedContainerColor = color.color,
                                        focusedTextColor = getInvertedColor(color.color)
                                    )
                                } else
                                    colors()
                            }
                        )
                        action = {
                            if (username.isNotBlank() && !checkColor(color)) {
                                isUsernameError = false
                                isColorError = false
                                viewModel
                                    .doCommand<Nothing>(
                                        INSERT_USER,
                                        User(name = username, color = color.color)
                                    )
                            } else {
                                hasTriedToDismiss = true
                                isUsernameError = username.isBlank()
                                isColorError = checkColor(color)
                            }
                            !(isUsernameError || isColorError)
                        }
                    } else {
                        //for message
                        var message by rememberSaveable { mutableStateOf("") }
                        var to by remember { mutableStateOf(currentUsers.firstOrNull()) }
                        var expanded by remember { mutableStateOf(false) }

                        var isMessageError by remember { mutableStateOf(false) }
                        var isUserChooseError by remember { mutableStateOf(to == null) }

                        if (hasTriedToDismiss && isMessageError)
                            ErrorText(stringResource(id = R.string.error_empty_message))
                        TextField(
                            value = message,
                            onValueChange = {
                                message = it
                                isMessageError = it.isBlank()
                            }
                        )

                        if (hasTriedToDismiss && isUserChooseError)
                            ErrorText(stringResource(R.string.error_user_is_not_selected))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                            }
                        ) {
                            TextField(
                                modifier = Modifier.menuAnchor(),
                                readOnly = true,
                                value = to?.name ?: stringResource(R.string.no_users),
                                onValueChange = { },
                                label = { Text("Для") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expanded
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {
                                    expanded = false
                                }
                            ) {
                                currentUsers.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption.name) },
                                        leadingIcon = { Icon(Icons.Default.AccountBox, null) },
                                        onClick = {
                                            to = selectionOption
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        action = {
                            if (message.isNotBlank() && to!=null) {
                                isMessageError = false
                                isUserChooseError = false
                                /*viewModel
                                    .doCommand<Nothing>(
                                        INSERT_MESSAGE,
                                        Message(
                                            text = message,
                                            check = true,
                                            datetime = Date(),
                                            userId = to!!.id,
                                            fromId = to!!.id
                                        )
                                    )*/
                            } else {
                                hasTriedToDismiss = true
                                isMessageError = message.isBlank()
                                isUserChooseError = to == null
                            }

                            !(isUserChooseError || isMessageError)
                        }
                    }
                }
            }
        },
        dismissButton = {
            Button(onClick = {
                viewModel.doCommand<Nothing>(SWITCH_DIALOG_OFF)
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            Button(onClick = {
                if (action())
                    viewModel.doCommand<Nothing>(SWITCH_DIALOG_OFF)
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}

@Composable
fun ErrorText(text: String) {
    Text(text = text, color = Color.Red, fontSize = 10.sp)
}

//false - если цвет верен
//true - если есть ошибки
fun checkColor(s: String): Boolean {

    val b1 = s.length in intArrayOf(6, 8)
    val b2 = s.lowercase().all { "0123456789abcdef".contains(it) }
    Log.i("checkColor", "checkColor: ${!(b1 && b2)}")
    return !(b1 && b2)
}

fun getInvertedColor(color: Color): Color {
    val red = 1.0f - color.red
    val green = 1.0f - color.green
    val blue = 1.0f - color.blue
    return Color(red, green, blue, alpha = color.alpha)
}
