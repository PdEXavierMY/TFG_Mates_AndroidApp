package com.example.tfgmates

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL

class RobotActivity : AppCompatActivity() {

    private lateinit var btnIrAtras2: Button
    private lateinit var popupInforme: LinearLayout
    private lateinit var btnPrograma1: Button
    private lateinit var btnPrograma2: Button
    private lateinit var btnPrograma3: Button
    private lateinit var btnCerrarPopup: Button
    private lateinit var btnMostrarPopup: Button

    private lateinit var logTextView: TextView
    private lateinit var logScrollView: ScrollView
    private lateinit var webView: WebView

    private var ipRobot = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.robot)
        enableEdgeToEdge()

        val id = intent.getLongExtra("id", -1L)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val wifi = intent.getStringExtra("wifi") ?: ""
        ipRobot = intent.getStringExtra("ip") ?: ""

        btnIrAtras2 = findViewById(R.id.buttonAtras2)
        btnMostrarPopup = findViewById(R.id.buttonListaProgramas)
        popupInforme = findViewById(R.id.popupInforme)
        btnPrograma1 = findViewById(R.id.btnPrograma1)
        btnPrograma2 = findViewById(R.id.btnPrograma2)
        btnPrograma3 = findViewById(R.id.btnPrograma3)
        btnCerrarPopup = findViewById(R.id.btnCerrar2)

        logTextView = findViewById(R.id.textLogs)
        logScrollView = findViewById(R.id.logScrollView)
        webView = findViewById(R.id.videoStreamView)

        // Mostrar popup
        btnMostrarPopup.setOnClickListener {
            popupInforme.visibility = LinearLayout.VISIBLE
        }

        // Cerrar popup
        btnCerrarPopup.setOnClickListener {
            popupInforme.visibility = LinearLayout.GONE
        }

        // Botón atrás
        btnIrAtras2.setOnClickListener {
            val intent = Intent(this, RobotMainActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("nombre", nombre)
            intent.putExtra("wifi", wifi)
            intent.putExtra("ip", ipRobot)
            startActivity(intent)
        }

        // Iniciar la escucha de logs
        startLogFetching()
    }

    private fun startLogFetching() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val log = URL("http://$ipRobot:5000/logs/latest").readText() // Ruta en tu server Flask
                    runOnUiThread {
                        logTextView.text = log
                        logScrollView.post {
                            logScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        logTextView.text = "Error al obtener logs: ${e.message}"
                    }
                }
                delay(2000) // cada 2 segundos
            }
        }
    }
}