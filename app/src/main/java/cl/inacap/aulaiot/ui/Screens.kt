package cl.inacap.aulaiot.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.inacap.aulaiot.AulaState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Pantalla de inicio de sesion y registro de usuarios
@Composable
fun LoginScreen(error: String?, onSubmit: (correo: String, pass: String, crear: Boolean) -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var crear by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text("Aula Inteligente", color = Color.White, fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Control del salon de clases", color = Color(0xFFD0D0D0), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = correo, onValueChange = { correo = it }, label = { Text("Correo") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth()
        )

        if (error != null) Text(error, color = Color(0xFFFFB4AB), fontSize = 14.sp)

        AulaButton(if (crear) "Crear cuenta" else "Iniciar sesion", primary = true) { onSubmit(correo.trim(), pass, crear) }

        TextButton(onClick = { crear = !crear }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (crear) "Ya tengo cuenta" else "Crear cuenta", color = Color.White)
        }
    }
}

// Componente visual para mostrar metricas individuales
@Composable
private fun Metric(label: String, value: String, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = CardColor), shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontSize = 20.sp)
            Text(label, color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CardTitle(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Text(text, color = Color.White, fontSize = 20.sp)
    }
}

// Pantalla principal de Clima y sensores del aula
@Composable
fun ClimaScreen(st: AulaState, onPower: () -> Unit, onLogout: () -> Unit, onDemo: () -> Unit) {
    AulaScreen("Salon de clases") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesion", color = Color.White)
            }
            OutlinedIconButton(onClick = onPower, modifier = Modifier.size(72.dp), border = BorderStroke(1.dp, Color(0xFF888888))) {
                Icon(
                    Icons.Default.PowerSettingsNew,
                    contentDescription = if (st.connected) "Desconectar" else "Conectar",
                    tint = Color.White, modifier = Modifier.size(36.dp)
                )
            }
        }

        Dial(
            big = st.temp?.let { "%.1f°C".format(it) } ?: "--",
            sub = if (st.connected) (if (st.status == "Modo demo") "Temperatura (demo)" else "Temperatura del salon") else st.status,
            lit = ((st.temp ?: 10f) - 10f) / 30f,
            modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.CenterHorizontally)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metric("Aire (gas)", st.gas?.toString() ?: "--", Modifier.weight(1f))
            Metric("Luz", st.luz?.toString() ?: "--", Modifier.weight(1f))
            Metric("Presencia", when (st.presencia) { true -> "Si"; false -> "No"; null -> "--" }, Modifier.weight(1f))
        }

        if (!st.connected) AulaButton("Probar en modo demo", onClick = onDemo)
    }
}

// Pantalla de control de equipos (extractor y luces)
@Composable
fun EquiposScreen(st: AulaState, onExtractor: (Boolean) -> Unit, onLuces: (Boolean) -> Unit, onTodo: (Boolean) -> Unit) {
    val manual = st.connected && !st.auto
    AulaScreen("Extractor y luces") {
        if (!manual) Text(if (st.auto) "Modo automatico activo: el control manual esta desactivado." else "Conecta el modulo Bluetooth para controlar los equipos.", color = Muted, fontSize = 14.sp)

        AulaCard {
            CardTitle(Icons.Default.Air, "Extractor")
            Segmented(listOf("Apagado", "Encendido"), if (st.extractor) 1 else 0, manual) { onExtractor(it == 1) }
        }
        AulaCard {
            CardTitle(Icons.Default.Lightbulb, "Luces")
            Segmented(listOf("Apagadas", "Encendidas"), if (st.luces) 1 else 0, manual) { onLuces(it == 1) }
        }

        ToggleCard("Todo el salon", "Extractor y luces a la vez", st.extractor && st.luces, manual, onTodo)
    }
}

// Pantalla de configuracion del modo automatico
@Composable
fun AutoScreen(st: AulaState, onAuto: (Boolean) -> Unit) {
    @Composable fun Regla(titulo: String, texto: String) = AulaCard {
        Text(titulo, color = Color.White, fontSize = 17.sp)
        Text(texto, color = Color(0xFFD0D0D0), fontSize = 15.sp)
    }

    AulaScreen("Modo automatico") {
        ToggleCard("Modo automatico", "El Arduino decide segun los sensores", st.auto, st.connected, onAuto)
        Regla("Extractor", "Abre la compuerta cuando el aire o la temperatura pasan el limite.")
        Regla("Ventana", "Se abre sola cuando la temperatura sube mas de lo permitido.")
        Regla("Luces", "Se encienden si hay gente en la sala y esta oscuro.")
    }
}

// Pantalla de control de la ventana
@Composable
fun VentanaScreen(st: AulaState, onVentana: (Boolean) -> Unit) {
    val manual = st.connected && !st.auto
    AulaScreen("Ventana") {
        Dial(
            big = if (st.ventana) "Abierta" else "Cerrada", sub = "Estado de la ventana",
            lit = if (st.ventana) 1f else 0f, bigSize = 44.sp,
            modifier = Modifier.fillMaxWidth(0.75f).align(Alignment.CenterHorizontally)
        )
        if (!manual) Text(if (st.auto) "Desactiva el modo automatico para abrir o cerrar a mano." else "Conecta el modulo Bluetooth para usar la ventana.", color = Muted, fontSize = 14.sp)

        AulaButton("Abrir", primary = true, enabled = manual && !st.ventana) { onVentana(true) }
        AulaButton("Cerrar", enabled = manual && st.ventana) { onVentana(false) }
    }
}

// Pantalla de historial de lecturas y base de datos
@Composable
fun DatosScreen(st: AulaState, onRefresh: () -> Unit) {
    val datos = st.historial
    val temps = datos.mapNotNull { it.temp }
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()) }

    AulaScreen("Historial de lecturas") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metric("Registros", datos.size.toString(), Modifier.weight(1f))
            Metric("Temp. prom.", if (temps.isEmpty()) "--" else "%.1f°".format(temps.average()), Modifier.weight(1f))
            Metric("Temp. max.", temps.maxOrNull()?.let { "%.1f°".format(it) } ?: "--", Modifier.weight(1f))
        }

        AulaButton("Actualizar", onClick = onRefresh)

        if (datos.isEmpty()) {
            Text("Aun no hay lecturas guardadas. Conectate al Arduino o usa el modo demo.", color = Muted, fontSize = 14.sp)
        } else {
            AulaCard {
                Row(Modifier.fillMaxWidth()) {
                    Text("Hora", color = Muted, fontSize = 12.sp, modifier = Modifier.weight(2.4f))
                    Text("Temp", color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text("Gas", color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Luz", color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Pers.", color = Muted, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                }
                datos.forEach { d ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(fmt.format(Date(d.ts)), color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(2.4f))
                        Text(d.temp?.let { "%.1f°".format(it) } ?: "--", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                        Text(d.gas?.toString() ?: "--", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(d.luz?.toString() ?: "--", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(when (d.presencia) { true -> "Si"; false -> "No"; null -> "--" }, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(0.9f))
                    }
                }
            }
        }
    }
}