package ru.gfastg98.sms_messenger

import android.provider.Telephony
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_QUEUED
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
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
import ru.gfastg98.sms_messenger.Commands.INSERT_SMS
import ru.gfastg98.sms_messenger.Commands.SWITCH_DIALOG_OFF
import ru.gfastg98.sms_messenger.room.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    viewModel: MessengerViewModel = viewModel(),
    contacts: List<User>
) {
    val context = LocalContext.current

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    var decision by remember { mutableStateOf(true) }

    var hasTriedToDismiss by remember { mutableStateOf(false) }

    var type by remember { mutableIntStateOf(Telephony.Sms.MESSAGE_TYPE_SENT) }
    var to by remember { mutableStateOf(contacts.firstOrNull()) }
    var newMessage by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            viewModel.doCommand<Nothing>(SWITCH_DIALOG_OFF)
        },
        title = { Text(text = stringResource(R.string.label_add)) },
        text = {
            Column {
                Column {
                    val options = listOf(
                        MESSAGE_TYPE_INBOX,
                        MESSAGE_TYPE_SENT,
                        MESSAGE_TYPE_DRAFT,
                        MESSAGE_TYPE_OUTBOX,
                        MESSAGE_TYPE_FAILED,
                        MESSAGE_TYPE_QUEUED
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded1,
                        onExpandedChange = {
                            expanded1 = !expanded1
                        }
                    ) {
                        TextField(
                            modifier = Modifier.menuAnchor(),
                            readOnly = true,
                            value = type.messageTypeName,
                            onValueChange = { },
                            label = { Text(stringResource(R.string.label_message_type)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded1
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded1,
                            onDismissRequest = {
                                expanded1 = false
                            }
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(option.messageTypeName)
                                    },
                                    leadingIcon = { Icon(option.messageTypeIcon, null) },
                                    onClick = {
                                        type = option
                                        expanded1 = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.size(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {// RadioGroup (контакты/новый)
                        val action1 = {
                            decision = true
                            hasTriedToDismiss = false
                            to = contacts.firstOrNull()
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
                            Text(text = stringResource(R.string.label_from_contacts))
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
                            Text(text = stringResource(R.string.label_new))
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
                            if (contacts.isEmpty() || to == null) {
                                ErrorText(text = stringResource(id = R.string.error_no_contacts))
                                Spacer(Modifier.size(8.dp))
                            } else {
                                Text(text = stringResource(R.string.label_select_user))
                                ExposedDropdownMenuBox(
                                    expanded = expanded2,
                                    onExpandedChange = {
                                        expanded2 = !expanded2
                                    }
                                ) {
                                    TextField(
                                        modifier = Modifier.menuAnchor(),
                                        readOnly = true,
                                        value = to!!.name,
                                        onValueChange = { },
                                        label = { Text(stringResource(R.string.label_for)) },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expanded2
                                            )
                                        },
                                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded2,
                                        onDismissRequest = {
                                            expanded2 = false
                                        }
                                    ) {
                                        contacts.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                text = { Text(selectionOption.name) },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountBox,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    to = selectionOption
                                                    expanded2 = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column {
                            var text by rememberSaveable { mutableStateOf("") }

                            if (hasTriedToDismiss && (text.isEmpty() || !text.isNumber))
                                ErrorText(text = stringResource(R.string.error_number))
                            TextField(
                                label = { Text(text = stringResource(R.string.label_phone_number)) },
                                value = text,
                                singleLine = true,
                                onValueChange = {
                                    if (it.isNumber) {
                                        text = it
                                        to!!.num = it
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.size(8.dp))

                if (hasTriedToDismiss && newMessage.isEmpty())
                    ErrorText(text = stringResource(id = R.string.error_empty_message))
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    label = { Text(text = stringResource(id = R.string.label_message_line)) }
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
                if( to == null || (to?.num?.isEmpty() ?: to?.name?.isEmpty()) == true || newMessage.isEmpty()) {
                    hasTriedToDismiss = true
                    return@Button
                }

                viewModel.doCommand<Nothing>(
                    INSERT_SMS,
                    context,
                    type,
                    to?.num?:to?.name?:"0",
                    newMessage.trim()
                )
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

