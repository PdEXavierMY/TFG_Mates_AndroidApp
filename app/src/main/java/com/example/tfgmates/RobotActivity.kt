package com.example.tfgmates

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tfgmates.helpers.HttpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class RobotActivity : AppCompatActivity() {

    private lateinit var btnIrAtras2: Button
    private lateinit var popupInforme: LinearLayout
    private lateinit var btnPrograma1: Button
    private lateinit var btnPrograma2: Button
    private lateinit var btnPrograma3: Button
    private lateinit var btnCerrarPopup: Button
    private lateinit var btnMostrarPopup: Button
    private lateinit var btnParar: Button
    private lateinit var btnRestart: Button
    private lateinit var btnCalibrar: Button

    private lateinit var logTextView: TextView
    private lateinit var logScrollView: ScrollView
    private lateinit var webView: WebView
    private var streamActivo = false


    private var ipServer = "192.168.1.41"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.robot)
        enableEdgeToEdge()

        val id = intent.getLongExtra("id", -1L)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val wifi = intent.getStringExtra("wifi") ?: ""
        val ip = intent.getStringExtra("ip") ?: ""

        btnIrAtras2 = findViewById(R.id.buttonAtras2)
        btnMostrarPopup = findViewById(R.id.buttonListaProgramas)
        popupInforme = findViewById(R.id.popupInforme)
        btnPrograma1 = findViewById(R.id.btnRojo)
        btnPrograma2 = findViewById(R.id.btnVerde)
        btnPrograma3 = findViewById(R.id.btnVideo)
        btnCerrarPopup = findViewById(R.id.btnCerrar2)
        btnParar = findViewById(R.id.btnPararR)
        btnRestart = findViewById(R.id.btnReiniciarR)
        btnCalibrar = findViewById(R.id.btnCalibrarR)

        logTextView = findViewById(R.id.textLogs)
        logScrollView = findViewById(R.id.logScrollView)
        webView = findViewById(R.id.videoStreamView)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

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
            webView.visibility = WebView.GONE
            logScrollView.visibility = ScrollView.VISIBLE
            if (streamActivo) {
                pararStream()
            }

            val intent = Intent(this, RobotMainActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("nombre", nombre)
            intent.putExtra("wifi", wifi)
            intent.putExtra("ip", ip)
            startActivity(intent)
        }

        btnPrograma1.setOnClickListener {
            webView.visibility = WebView.GONE
            logScrollView.visibility = ScrollView.VISIBLE
            if (streamActivo) {
                pararStream()
            }
            ejecutarPrograma("programa_base_rojo")
            popupInforme.visibility = LinearLayout.GONE
        }

        btnPrograma2.setOnClickListener {
            webView.visibility = WebView.GONE
            logScrollView.visibility = ScrollView.VISIBLE
            if (streamActivo) {
                pararStream()
            }
            ejecutarPrograma("programa_base_verde")
            popupInforme.visibility = LinearLayout.GONE
        }

        btnPrograma3.setOnClickListener {
            // Marcar como activo
            streamActivo = true
            val videoUrl = "http://$ipServer:5000/video_feed"
            webView.loadUrl(videoUrl)
            webView.visibility = WebView.VISIBLE
            logScrollView.visibility = ScrollView.GONE
            // Llamada al stream
            enviarPeticion("/stream")
            popupInforme.visibility = LinearLayout.GONE
        }

        btnParar.setOnClickListener {
            webView.visibility = WebView.GONE
            logScrollView.visibility = ScrollView.VISIBLE
            if (streamActivo) {
                pararStream()
            }
            enviarNotificacion("/parar")
            popupInforme.visibility = LinearLayout.GONE
        }

        btnRestart.setOnClickListener {
            webView.visibility = WebView.GONE
            logScrollView.visibility = ScrollView.VISIBLE
            if (streamActivo) {
                pararStream()
            }
            enviarPeticion("/restart")
            popupInforme.visibility = LinearLayout.GONE
        }

        btnCalibrar.setOnClickListener {
            webView.visibility = WebView.GONE
            logScrollView.visibility = ScrollView.VISIBLE
            if (streamActivo) {
                pararStream()
            }
            enviarPeticion("/calibrar")
            popupInforme.visibility = LinearLayout.GONE
        }

        // Iniciar la escucha de logs
        startLogFetching()
    }

    private fun ejecutarPrograma(nombrePrograma: String) {
        val url = "http://$ipServer:5000/ejecutar"
        val json = JSONObject().apply {
            put("programa", nombrePrograma)
        }

        HttpHelper.sendHttpRequest(url, json) { response ->
            runOnUiThread {
                popupInforme.visibility = LinearLayout.GONE
                if (response != null) {
                    logTextView.text = "Ejecutado: $nombrePrograma\nRespuesta: $response"
                } else {
                    logTextView.text = "Error al ejecutar $nombrePrograma"
                }
            }
        }
    }

    private fun enviarPeticion(endpoint: String) {
        val url = "http://$ipServer:5000$endpoint"
        val json = JSONObject()

        HttpHelper.sendHttpRequest(url, json) { response ->
            runOnUiThread {
                if (response != null) {
                    logTextView.text = "Respuesta de $endpoint:\n$response"
                } else {
                    logTextView.text = "Error en $endpoint"
                }
            }
        }
    }

    private fun enviarNotificacion(endpoint: String) {
        val url = "http://$ipServer:5000$endpoint"
        val json = JSONObject()

        HttpHelper.sendHttpNotice(url, json) { success ->
            runOnUiThread {
                logTextView.text = if (success) {
                    "Notificación $endpoint enviada con éxito"
                } else {
                    "Error al enviar notificación $endpoint"
                }
            }
        }
    }

    private fun startLogFetching() {
        lifecycleScope.launch {
            while (true) {
                val url = "http://$ipServer:5000/logs/latest"
                HttpHelper.getHttpText(url) { response ->
                    if (response != null) {
                        try {
                            val json = JSONObject(response)
                            val log = json.opt("log")
                            if (log != null && log != false) {
                                runOnUiThread {
                                    logTextView.text = log.toString()
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                logTextView.text = "Error al analizar logs: ${e.message}"
                            }
                        }
                    }
                }
                delay(2000)
            }
        }
    }

    private fun pararStream() {
        val url = "http://$ipServer:5000/parar_stream"
        val json = JSONObject()

        HttpHelper.sendHttpRequest(url, json) { response ->
            runOnUiThread {
                if (response != null) {
                    logTextView.text = "Stream detenido"
                } else {
                    logTextView.text = "Error al detener stream"
                }
                streamActivo = false
            }
        }
    }
}