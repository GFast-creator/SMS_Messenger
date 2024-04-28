package ru.gfastg98.sms_messenger.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.gfastg98.sms_messenger.MessengerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    viewModel: MessengerViewModel
) {
    AlertDialog(onDismissRequest = { viewModel.doCommand<Nothing>(MessengerViewModel.Command.SWITCH_DIALOG_OFF) } ) {

    }
}