package ru.gfastg98.sms_messenger.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.gfastg98.sms_messenger.R

@Composable
fun DeleteDialog(
    onDismissAction : () -> Unit,
    onConfirmAction: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = stringResource(R.string.label_delete_dialog)) },
        onDismissRequest = onDismissAction,
        confirmButton = {
            Button(onClick = onConfirmAction) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismissAction) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}
