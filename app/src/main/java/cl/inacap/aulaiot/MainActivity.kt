package cl.inacap.aulaiot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.inacap.aulaiot.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AulaTheme { AulaApp() } }
    }
}

@Composable
fun AulaApp(vm: AulaViewModel = viewModel()) {
    val st by vm.ui.collectAsState()
    Box(Modifier.fillMaxSize().aulaBackground()) {
        if (!st.loggedIn) LoginScreen(st.authError, vm::login) else Principal(vm, st)
    }
}

private data class Tab(val label: String, val icon: ImageVector)

// Vista principal con la barra de navegacion inferior y cambio de pantallas
@Composable
private fun Principal(vm: AulaViewModel, st: AulaState) {
    val tabs = listOf(
        Tab("Clima", Icons.Default.Thermostat), Tab("Equipos", Icons.Default.Air),
        Tab("Auto", Icons.Default.AutoMode), Tab("Ventana", Icons.Default.Window),
        Tab("Datos", Icons.Default.History)
    )
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var verDispositivos by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        verDispositivos = true
    }

    // Valida permisos de Bluetooth antes de mostrar los dispositivos emparejados
    fun alPulsarEncendido() {
        if (st.connected) { vm.disconnect(); return }
        val falta = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        if (falta) pedirPermiso.launch(Manifest.permission.BLUETOOTH_CONNECT) else verDispositivos = true
    }

    LaunchedEffect(tab) { if (tab == 4) vm.cargarHistorial() }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = BarColor) {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { tab = i },
                        icon = { Icon(t.icon, contentDescription = null) }, label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, selectedTextColor = Color.White,
                            unselectedIconColor = Color(0xFFA0A0A0), unselectedTextColor = Color(0xFFA0A0A0),
                            indicatorColor = Color(0xFF444444)
                        )
                    )
                }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            when (tab) {
                0 -> ClimaScreen(st, ::alPulsarEncendido, vm::logout, vm::startDemo)
                1 -> EquiposScreen(st, vm::setExtractor, vm::setLuces, vm::setTodo)
                2 -> AutoScreen(st, vm::setAuto)
                3 -> VentanaScreen(st, vm::setVentana)
                else -> DatosScreen(st, vm::cargarHistorial)
            }
        }
    }

    // Modal para seleccionar el dispositivo Bluetooth o iniciar el modo demo
    if (verDispositivos) {
        val dispositivos = remember { vm.pairedDevices() }
        AlertDialog(
            onDismissRequest = { verDispositivos = false },
            title = { Text("Conectar") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (dispositivos.isEmpty()) {
                        Text("No hay dispositivos vinculados. Vincula el modulo desde Ajustes > Bluetooth del telefono.")
                    }
                    dispositivos.forEach { d ->
                        TextButton(onClick = { vm.connect(d); verDispositivos = false }) { Text(d.name) }
                    }
                    TextButton(onClick = { vm.startDemo(); verDispositivos = false }) { Text("Modo demo (sin Arduino)") }
                }
            },
            confirmButton = { TextButton(onClick = { verDispositivos = false }) { Text("Cancelar") } }
        )
    }
}