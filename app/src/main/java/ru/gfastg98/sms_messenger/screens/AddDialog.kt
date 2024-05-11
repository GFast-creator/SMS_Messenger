package ru.gfastg98.sms_messenger.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ru.gfastg98.sms_messenger.Commands.ADD_USER
import ru.gfastg98.sms_messenger.Commands.GET_CONTACTS
import ru.gfastg98.sms_messenger.Commands.SEND_SMS
import ru.gfastg98.sms_messenger.Commands.SWITCH_DIALOG_OFF
import ru.gfastg98.sms_messenger.MessengerViewModel
import ru.gfastg98.sms_messenger.R
import ru.gfastg98.sms_messenger.ROUTS
import ru.gfastg98.sms_messenger.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    viewModel: MessengerViewModel = viewModel(),
    navController: NavHostController,
    contacts : List<User>
) {
    val context = LocalContext.current

    //val currentUsers by viewModel.smsTable.collectAsState()

    var decision by remember {
        mutableStateOf(true)
    }
    //var action: (() -> Unit) = { }

    var hasTriedToDismiss by remember { mutableStateOf(false) }

    if (contacts.isEmpty()) {
        Text(text = "Не найдено ни одного контакта")
        return
    }
    var to by remember {
        mutableStateOf<User>(contacts.first())
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
                    Row(
                        Modifier
                            .clickable {
                                decision = true
                                hasTriedToDismiss = false
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Из контактов")
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
                        Text(text = "Новый")
                        RadioButton(
                            selected = !decision,
                            onClick = {
                                decision = false
                                hasTriedToDismiss = false
                            }
                        )
                    }
                }

                if (decision)
                    Column {
                        Text(text = "Выберите пользователя из контактов")
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                            }
                        ) {
                            TextField(
                                modifier = Modifier.menuAnchor(),
                                readOnly = true,
                                value = to.name ?: stringResource(R.string.no_users),
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
                                contacts
                                    .forEach { selectionOption ->
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
                else
                    to = User()
                Column {
                    var text by rememberSaveable {
                        mutableStateOf("")
                    }
                    TextField(
                        value = text,
                        onValueChange = {
                            text = it
                            to.name = it
                        }
                    )
                }
            }

            OutlinedTextField(value = newMessage, onValueChange = {newMessage = it})
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

                viewModel.doCommand<Nothing>(SEND_SMS, data = arrayOf(context, newMessage, to.name))
                viewModel.doCommand<Nothing>(SWITCH_DIALOG_OFF)

                Toast.makeText(context, "Сообщение было отправлено", Toast.LENGTH_LONG).show()
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

