package ru.gfastg98.sms_messenger

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun ErrorText(text: String) {
    Text(text = text, color = Color.Red, fontSize = 10.sp)
}

