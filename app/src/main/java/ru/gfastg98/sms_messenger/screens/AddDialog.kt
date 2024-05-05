package ru.gfastg98.sms_messenger.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import ru.gfastg98.sms_messenger.MessengerViewModel
import ru.gfastg98.sms_messenger.User
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.gfastg98.sms_messenger.Message
import ru.gfastg98.sms_messenger.R
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    viewModel: MessengerViewModel = viewModel()
) {
    val currentUsers by viewModel
        .doCommand<Flow<List<User>>>(MessengerViewModel.Commands.GET_USERS)!!
        .collectAsState(initial = emptyList())
    var decision by remember {
        mutableStateOf(true)
    }
    var action = {}
    AlertDialog(
        onDismissRequest = {
            viewModel.doCommand<Nothing>(MessengerViewModel.Commands.SWITCH_DIALOG_OFF)
                           },
        title = { Text(text = if (decision) "Новый пользователь" else "Новое сообщение")},
        text = {
            Column {

                Row {
                    RadioButton(selected = decision, onClick = { decision = true })
                    RadioButton(selected = !decision, onClick = { decision = false })
                }

                Column {
                    if (decision) {
                        //for user
                        var userName by rememberSaveable { mutableStateOf("") }
                        var color by rememberSaveable { mutableStateOf("0") }

                        TextField(
                            label = { Text("Имя пользователя")},
                            value = userName, onValueChange = { userName = it })
                        TextField(
                            label = { Text("Цвет")},
                            value = color, onValueChange = { color = it },
                            /*colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(color.toULong()),
                                unfocusedContainerColor = Color(color.toULong()),
                            )*/
                        )
                        action = {
                            viewModel
                                .doCommand<Nothing>(
                                    MessengerViewModel.Commands.INSERT_USER,
                                    User(name = userName, color = color.toULongOrNull()
                                        ?.let { Color(it) } ?: Color(0))
                                )
                        }
                    } else {

                        //for message
                        var message by rememberSaveable { mutableStateOf("") }
                        val to by rememberSaveable { mutableStateOf<User?>(null) }

                        TextField(value = message, onValueChange = { message = it })
                        DropDownMenuUsers(obj = currentUsers)
                        repeat(5){}

                        action = {
                            viewModel
                                .doCommand<Nothing>(
                                    MessengerViewModel.Commands.INSERT_MESSAGE,
                                    Message(text = message, check = true, datetime = Date(), userId = to!!.id)
                                )
                        }
                    }
                }
            }
        },
        dismissButton = {
            Button(onClick = {
                viewModel.doCommand<Nothing>(MessengerViewModel.Commands.SWITCH_DIALOG_OFF)
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            Button(onClick = {
                action()
                viewModel.doCommand<Nothing>(MessengerViewModel.Commands.SWITCH_DIALOG_OFF)
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewDropDownMenuUsers() {
    DropDownMenuUsers(obj = listOf(User(name = "123")))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenuUsers(
    modifier: Modifier = Modifier,
    obj: List<User>
) {
    if (obj.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(obj.first()) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
            readOnly = true,
            value = selectedOptionText.name,
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
            obj.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.name) },
                    leadingIcon = { Icon(Icons.Default.AccountBox, null) },
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false

                    }
                )
            }
        }
    }
}
