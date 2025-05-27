package com.example.tfgmates

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tfgmates.ReportActivity
import com.example.tfgmates.bbdd.AppDatabase
import kotlinx.coroutines.launch

class RobotMainActivity : AppCompatActivity() {

    private lateinit var btnIrAtras: Button
    private lateinit var btnProgramarRobot: Button
    private lateinit var btnReportarIncidencia: Button
    private lateinit var btnEstadisticas: Button
    private lateinit var labelNombre: TextView
    private lateinit var labelIP: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.robot_main)
        enableEdgeToEdge()

        // Obtener los datos del Intent
        val id = intent.getLongExtra("id", -1L)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val wifi = intent.getStringExtra("wifi") ?: ""
        val ip = intent.getStringExtra("ip") ?: ""

        // Referencias a los TextView
        val textId = findViewById<TextView>(R.id.textId)
        val textNombre = findViewById<TextView>(R.id.textNombre)
        val textWifi = findViewById<TextView>(R.id.textWifi)
        val textIp = findViewById<TextView>(R.id.textIp)

        // Asignar valores
        textId.text = String.format(id.toString())
        textNombre.text = nombre
        textWifi.text = wifi
        textIp.text = ip

        // Botón de "Ir Atrás"
        btnIrAtras = findViewById(R.id.buttonAtras)
        btnIrAtras.setOnClickListener {
            // Crear un Intent para ir a la actividad principal
            val intent = Intent(this, MainActivity::class.java)

            // Usar FLAG_ACTIVITY_CLEAR_TOP para asegurar que la actividad principal se recargue
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

            // Iniciar la actividad principal
            startActivity(intent)

            // Cerrar la actividad actual
            finish()
        }

        labelNombre = findViewById(R.id.headerNombre)
        labelIP = findViewById(R.id.headerIp)

        // Evento de click en "Nombre"
        labelNombre.setOnClickListener {
            showEditDialog("Nombre", textNombre.text.toString()) { nuevoNombre ->
                textNombre.text = nuevoNombre
                // Actualizar la base de datos con el nuevo nombre
                actualizarRobotEnBD(nuevoNombre = nuevoNombre)
            }
        }

        // Evento de click en "IP"
        labelIP.setOnClickListener {
            showEditDialog("IP", textIp.text.toString()) { nuevaIp ->
                if (esIpValida(nuevaIp)) {
                    textIp.text = nuevaIp
                    actualizarRobotEnBD(nuevaIp = nuevaIp)
                } else {
                    Toast.makeText(this, "IP no válida. Introduce una IP válida (ej: 192.168.1.100)", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnProgramarRobot = findViewById(R.id.buttonProgramas)
        btnProgramarRobot.setOnClickListener {
            val intent = Intent(this, RobotActivity::class.java)

            intent.putExtra("id", id)
            intent.putExtra("nombre", nombre)
            intent.putExtra("wifi", wifi)
            intent.putExtra("ip", ip)

            startActivity(intent)
        }

        btnReportarIncidencia = findViewById(R.id.buttonReportarIncidencia)
        btnReportarIncidencia.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)

            intent.putExtra("id", id)
            intent.putExtra("nombre", nombre)
            intent.putExtra("wifi", wifi)
            intent.putExtra("ip", ip)

            startActivity(intent)
        }

        btnEstadisticas = findViewById(R.id.buttonEstadisticas)
        btnEstadisticas.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)

            intent.putExtra("id", id)
            intent.putExtra("nombre", nombre)
            intent.putExtra("wifi", wifi)
            intent.putExtra("ip", ip)

            startActivity(intent)
        }
    }

    fun esIpValida(ip: String): Boolean {
        val ipRegex = Regex("^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\$")
        return ipRegex.matches(ip)
    }

    // Función para mostrar el cuadro de diálogo de edición
    private fun showEditDialog(title: String, currentText: String, onTextChanged: (String) -> Unit) {
        val editText = EditText(this)
        editText.setText(currentText)

        AlertDialog.Builder(this)
            .setTitle("Editar $title")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val newText = editText.text.toString()
                onTextChanged(newText)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Función para actualizar los datos del robot en la base de datos
    private fun actualizarRobotEnBD(nuevoNombre: String? = null, nuevaIp: String? = null) {
        val robotDao = AppDatabase.getInstance(this).wifiDao()

        lifecycleScope.launch {
            val id = intent.getLongExtra("id", -1L)
            val robot = robotDao.getById(id)
            robot?.let {
                if (nuevoNombre != null) it.nombre = nuevoNombre
                if (nuevaIp != null) it.ip = nuevaIp
                robotDao.update(it) // Actualizar la base de datos con los cambios
            }
        }
    }
}