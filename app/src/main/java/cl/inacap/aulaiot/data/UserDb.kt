package cl.inacap.aulaiot.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class Lectura(val ts: Long, val temp: Float?, val gas: Int?, val luz: Int?, val presencia: Boolean?)

// bbdd para usuarios y lecturas de sensores
class UserDb(ctx: Context) : SQLiteOpenHelper(ctx, "aula.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios(correo TEXT PRIMARY KEY, sal TEXT NOT NULL, hash TEXT NOT NULL)")
        db.execSQL("CREATE TABLE lecturas(id INTEGER PRIMARY KEY AUTOINCREMENT, ts INTEGER NOT NULL, temp REAL, gas INTEGER, luz INTEGER, presencia INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    // encriptacion de contrasena con PBKDF2
    private fun hash(pass: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(pass.toCharArray(), salt, 120_000, 256)).encoded

    private fun b64(b: ByteArray) = Base64.encodeToString(b, Base64.NO_WRAP)

    // registro de usuario y validacion de correo y clave
    fun register(correo: String, pass: String): Boolean {
        if (correo.isBlank() || pass.length < 6) return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val v = ContentValues().apply {
            put("correo", correo.lowercase())
            put("sal", b64(salt))
            put("hash", b64(hash(pass, salt)))
        }
        return writableDatabase.insert("usuarios", null, v) != -1L
    }

    // valida el login comparando el hash almacenado con la clave ingresada
    fun login(correo: String, pass: String): Boolean =
        readableDatabase.rawQuery("SELECT sal, hash FROM usuarios WHERE correo = ?", arrayOf(correo.lowercase())).use { c ->
            if (!c.moveToFirst()) {
                false
            } else {
                val salt = Base64.decode(c.getString(0), Base64.NO_WRAP)
                val esperado = Base64.decode(c.getString(1), Base64.NO_WRAP)
                MessageDigest.isEqual(esperado, hash(pass, salt))
            }
        }

    // guarda los datos que mandan los sensores
    fun saveReading(ts: Long, temp: Float?, gas: Int?, luz: Int?, presencia: Boolean?) {
        val v = ContentValues().apply {
            put("ts", ts)
            put("temp", temp)
            put("gas", gas)
            put("luz", luz)
            put("presencia", presencia?.let { if (it) 1 else 0 })
        }
        writableDatabase.insert("lecturas", null, v)
    }

    // obtiene las ultimas lecturas para mostrarlas en la vista
    fun lastReadings(limit: Int = 50): List<Lectura> {
        val out = mutableListOf<Lectura>()
        readableDatabase.rawQuery("SELECT ts, temp, gas, luz, presencia FROM lecturas ORDER BY ts DESC LIMIT $limit", null).use { c ->
            while (c.moveToNext()) {
                out += Lectura(
                    c.getLong(0),
                    if (c.isNull(1)) null else c.getFloat(1),
                    if (c.isNull(2)) null else c.getInt(2),
                    if (c.isNull(3)) null else c.getInt(3),
                    if (c.isNull(4)) null else c.getInt(4) == 1
                )
            }
        }
        return out
    }
}