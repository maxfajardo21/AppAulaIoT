package cl.inacap.aulaiot.ui

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BgTop = Color(0xFF5C5C5C)
val BgBottom = Color(0xFF2E2E2E)
val CardColor = Color(0xFF3A3A3A)
val BarColor = Color(0xFF262626)
val Muted = Color(0xFFC4C4C4)

@Composable
fun AulaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color.White,
            onPrimary = Color(0xFF222222),
            surface = CardColor,
            background = BgBottom
        ),
        content = content
    )
}

fun Modifier.aulaBackground(): Modifier =
    this.background(Brush.verticalGradient(listOf(BgTop, BgBottom)))