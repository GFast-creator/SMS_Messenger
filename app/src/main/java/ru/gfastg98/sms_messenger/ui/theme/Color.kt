package ru.gfastg98.sms_messenger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun getInvertedColor(color: Color): Color {
    if (color == Color.Unspecified) return MaterialTheme.colorScheme.primary
    val red = 1.0f - color.red
    val green = 1.0f - color.green
    val blue = 1.0f - color.blue
    return Color(red, green, blue, alpha = color.alpha)
}

val colorPool = listOf(
    Color(0xFF8f4c38),
    Color(0xFF4c662b),
    Color(0xFF415f91),
    Color(0xFFf8e287),
    Color(0xFFd6e3ff),
    Color(0xFFffdbd1),
)

val checkColor = Color(0xFF1E88E5)

val ItemColorRed = Color(0xFFFF5757)
val ItemColorGreen = Color(0xFFA8E4A0)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)