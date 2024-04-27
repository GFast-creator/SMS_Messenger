package ru.gfastg98.sms_messenger

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    userId: Long?,
    modifier: Modifier
) {
    if (userId == null){
        Text(text = "Пользователь не найден")
        return
    }
    //val messages by viewModel.table.collectAsState(initial = emptyMap())

    LazyColumn(modifier = modifier){

    }
}