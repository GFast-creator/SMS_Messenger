package ru.gfastg98.sms_messenger.ui.theme.shapes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class SpeechBubbleShape(
    private val cornerRadius: Dp = 15.dp,
    private val tipSize: Dp = 12.dp,
    private val isInboxMessage : Boolean = true,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadius = with(density){cornerRadius.toPx()}
        val tipSize = with(density){tipSize.toPx()}
        val path1 = Path().apply {
            addRoundRect(
                RoundRect(
                    left = if (isInboxMessage) tipSize else 0f,
                    top = 0f,
                    right = if (isInboxMessage) size.width else size.width - tipSize,
                    bottom = size.height,
                    radiusX = cornerRadius,
                    radiusY = cornerRadius
                )
            )

        }
        val path2 = Path().apply {
            if (isInboxMessage) {
                moveTo(
                    x = tipSize,
                    y = size.height - cornerRadius
                )
                cubicTo(
                    tipSize,
                    size.height - tipSize / 2,
                    tipSize / 2,
                    size.height,
                    0f,
                    size.height

                )
                lineTo(
                    x = cornerRadius * 2,
                    y = size.height
                )
            } else {
                moveTo(
                    x = size.width - tipSize,
                    y = size.height - cornerRadius
                )
                cubicTo(
                    size.width - tipSize,
                    size.height - tipSize/2,
                    size.width-tipSize/2,
                    size.height,
                    size.width,
                    size.height
                )
                lineTo(
                    x = size.width - cornerRadius * 2,
                    y = size.height
                )
            }
            close()
        }

        return Outline.Generic(
            path = Path().apply { op(path1, path2, PathOperation.Union) }
        )
    }
}

@Preview
@Composable
private fun ShapePreview() {
    Box(modifier = Modifier
        .clip(SpeechBubbleShape(isInboxMessage = false))
        .size(200.dp)
        .background(Color.Red)
    ){
        Text(text = "test")
    }
}