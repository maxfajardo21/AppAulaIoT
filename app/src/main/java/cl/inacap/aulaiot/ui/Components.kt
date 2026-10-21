package cl.inacap.aulaiot.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// Pantalla base: titulo centrado y contenido con scroll
@Composable
fun AulaScreen(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(title, color = Color.White, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        content()
    }
}

// Dial de rayitas estilo tacometro para las metricas
@Composable
fun Dial(big: String, sub: String, lit: Float, modifier: Modifier = Modifier, bigSize: TextUnit = 56.sp) {
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = size.minDimension / 300f
            val n = 60
            val litCount = (lit.coerceIn(0f, 1f) * n).toInt()
            for (i in 0 until n) {
                val a = Math.toRadians(135.0 + 270.0 * i / (n - 1))
                val dx = cos(a).toFloat()
                val dy = sin(a).toFloat()
                drawLine(
                    color = if (i < litCount) Color.White else Color(0xFF8A8A8A),
                    start = Offset(center.x + 118f * s * dx, center.y + 118f * s * dy),
                    end = Offset(center.x + 140f * s * dx, center.y + 140f * s * dy),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(big, color = Color.White, fontSize = bigSize)
            Text(sub, color = Color(0xFFD0D0D0), fontSize = 15.sp)
        }
    }
}

// Tarjeta generica reutilizable con estilo oscuro
@Composable
fun AulaCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

// Tarjeta con interruptor (switch) integrado
@Composable
fun ToggleCard(label: String, sub: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    AulaCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(label, color = Color.White, fontSize = 18.sp)
                Text(sub, color = Muted, fontSize = 14.sp)
            }
            Switch(
                checked = checked, onCheckedChange = onChange, enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color.White, checkedThumbColor = Color(0xFF333333),
                    uncheckedTrackColor = Color(0xFF6A6A6A), uncheckedThumbColor = Color(0xFFDDDDDD),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

// Botones segmentados (pestanas horizontales de seleccion)
@Composable
fun Segmented(options: List<String>, selected: Int, enabled: Boolean = true, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, label ->
            val m = Modifier.weight(1f).heightIn(min = 48.dp)
            if (i == selected) {
                Button(
                    onClick = { onSelect(i) }, enabled = enabled, modifier = m, shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF222222))
                ) { Text(label) }
            } else {
                OutlinedButton(
                    onClick = { onSelect(i) }, enabled = enabled, modifier = m, shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF777777)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text(label) }
            }
        }
    }
}

// Boton principal o secundario estandar para la app
@Composable
fun AulaButton(text: String, primary: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    val m = Modifier.fillMaxWidth().heightIn(min = 52.dp)
    if (primary) {
        Button(
            onClick = onClick, enabled = enabled, modifier = m, shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF222222))
        ) { Text(text, fontSize = 17.sp) }
    } else {
        OutlinedButton(
            onClick = onClick, enabled = enabled, modifier = m, shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF888888)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) { Text(text, fontSize = 17.sp) }
    }
}
