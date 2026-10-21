package cl.inacap.aulaiot.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

data class Dev(val name: String, val address: String)

// Bluetooth para mas adelante
class BluetoothClient(context: Context) {
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val spp: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var socket: BluetoothSocket? = null
    private var output: OutputStream? = null

    @SuppressLint("MissingPermission")
    fun paired(): List<Dev> =
        runCatching { adapter?.bondedDevices.orEmpty().map { Dev(it.name ?: it.address, it.address) } }
            .getOrDefault(emptyList())

    @SuppressLint("MissingPermission")
    fun connectAndRead(address: String, onConnected: () -> Unit): Flow<String> = flow {
        val bt = adapter ?: throw IOException("Este equipo no tiene Bluetooth")
        val s = bt.getRemoteDevice(address).createRfcommSocketToServiceRecord(spp)
        bt.cancelDiscovery()
        try {
            s.connect()
            socket = s
            output = s.outputStream
            onConnected()
            val reader = BufferedReader(InputStreamReader(s.inputStream))
            while (true) {
                val line = reader.readLine() ?: break
                emit(line)
            }
        } finally {
            close()
            runCatching { s.close() }
        }
    }.flowOn(Dispatchers.IO)

    fun send(cmd: String) {
        try {
            output?.apply { write((cmd + "\n").toByteArray()); flush() }
        } catch (_: IOException) { }
    }

    fun close() {
        runCatching { socket?.close() }
        socket = null
        output = null
    }
}