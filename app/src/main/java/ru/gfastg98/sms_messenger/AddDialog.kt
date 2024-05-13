package ru.gfastg98.sms_messenger

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ru.gfastg98.sms_messenger.Commands.SEND_SMS
import ru.gfastg98.sms_messenger.Commands.SWITCH_DIALOG_OFF

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    viewModel: MessengerViewModel = viewModel(),
    navController: NavHostController,
    contacts: List<User>
) {
    val context = LocalContext.current

    var decision by remember {
        mutableStateOf(true)
    }

    var hasTriedToDismiss by remember { mutableStateOf(false) }

    var to by remember {
        mutableStateOf(contacts.firstOrNull())
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    var newMessage by rememberSaveable {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.doCommand<Nothing>(SWITCH_DIALOG_OFF)
        },
        title = { Text(text = stringResource(R.string.add)) },
        text = {
            Column {
                Row {
                    val action1 = {
                        decision = true
                        hasTriedToDismiss = false
                        to = contacts.first()
                    }
                    val action2 = {
                        decision = false
                        hasTriedToDismiss = false
                        to = User(name = "")
                    }
                    Row(
                        Modifier
                            .clickable {
                                action1()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Из контактов")
                        RadioButton(selected = decision,
                            onClick = {
                                action1()
                            }
                        )
                    }
                    Row(
                        Modifier.clickable {
                            action2()
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Новый")
                        RadioButton(
                            selected = !decision,
                            onClick = {
                                action2()
                            }
                        )
                    }
                }

                if (decision) {
                    Column {
                        Text(text = "Выберите пользователя из контактов")
                        if (contacts.isEmpty()){
                            ErrorText(text = stringResource(id = R.string.error_no_users))
                            return@AlertDialog
                        }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                            }
                        ) {
                            TextField(
                                modifier = Modifier.menuAnchor(),
                                readOnly = true,
                                value = to!!.name,
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
                                contacts.forEach { selectionOption ->
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
                    }
                } else {
                    Column {
                        var text by rememberSaveable {
                            mutableStateOf("")
                        }
                        if (hasTriedToDismiss && (text.isEmpty() || !text.isNumber))
                            ErrorText(text = stringResource(R.string.error_number))
                        TextField(
                            value = text,
                            onValueChange = {
                                text = it
                                to!!.num = it
                            }
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
                if (hasTriedToDismiss && newMessage.isEmpty())
                    ErrorText(text = stringResource(id = R.string.error_empty_message))
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    label = { Text(text = stringResource(id = R.string.message_line)) }
                )
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
                if ((to?.num?.isEmpty() ?: to?.name?.isEmpty()) == true || newMessage.isEmpty()) {
                    hasTriedToDismiss = true
                    return@Button
                }
                viewModel.doCommand<Nothing>(SEND_SMS, data = arrayOf(context, newMessage, to!!.num?:to!!.name, !decision))
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

