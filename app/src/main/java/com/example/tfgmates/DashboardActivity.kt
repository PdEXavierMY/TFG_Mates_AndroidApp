package com.example.tfgmates

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.net.URL
import java.net.HttpURLConnection


class DashboardActivity: AppCompatActivity() {

    private lateinit var btnIrAtras4: Button
    private lateinit var imgGrafico: ImageView
    private lateinit var btnGrafico1: Button
    private lateinit var btnGrafico2: Button
    private lateinit var btnGrafico3: Button
    private lateinit var btnGrafico4: Button
    private lateinit var btnGrafico5: Button
    private lateinit var btnGrafico6: Button

    //private var ipServer = "192.168.32.134"
    private var ipServer = "192.168.1.41"
    private var serverport = "5000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)
        enableEdgeToEdge()

        val id = intent.getLongExtra("id", -1L)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val wifi = intent.getStringExtra("wifi") ?: ""
        val ip = intent.getStringExtra("ip") ?: ""

        btnIrAtras4 = findViewById(R.id.buttonAtras4)

        val popupGraficos = findViewById<LinearLayout>(R.id.popupGraficos)
        val btnMostrarPopup = findViewById<Button>(R.id.ListarGraficas)
        val btnCerrarPopup = findViewById<Button>(R.id.btnCerrar3)
        imgGrafico = findViewById(R.id.imageView2)
        btnGrafico1 = findViewById(R.id.ejecucionesprograma)
        btnGrafico2 = findViewById(R.id.tiempomedio)
        btnGrafico3 = findViewById(R.id.actividaddiaria)
        btnGrafico4 = findViewById(R.id.erroresporcodigo)
        btnGrafico5 = findViewById(R.id.erroresporprograma)
        btnGrafico6 = findViewById(R.id.evoluciontiempo)

        btnGrafico1.setOnClickListener {
            cargarGrafico("imagen_ejecuciones_programa")
            popupGraficos.visibility = View.GONE
        }
        btnGrafico2.setOnClickListener {
            cargarGrafico("imagen_tiempo_medio_programa")
            popupGraficos.visibility = View.GONE
        }
        btnGrafico3.setOnClickListener {
            cargarGrafico("imagen_proporcion_activo_inactivo")
            popupGraficos.visibility = View.GONE
        }
        btnGrafico4.setOnClickListener {
            cargarGrafico("imagen_errores_por_tipo")
            popupGraficos.visibility = View.GONE
        }
        btnGrafico5.setOnClickListener {
            cargarGrafico("imagen_errores_por_programa")
            popupGraficos.visibility = View.GONE
        }
        btnGrafico6.setOnClickListener {
            cargarGrafico("imagen_tiempo_activo_diario")
            popupGraficos.visibility = View.GONE
        }

        btnMostrarPopup.setOnClickListener {
            popupGraficos.visibility = View.VISIBLE
        }

        btnCerrarPopup.setOnClickListener {
            popupGraficos.visibility = View.GONE
        }

        btnIrAtras4 = findViewById(R.id.buttonAtras4)
        btnIrAtras4.setOnClickListener {
            val intent = Intent(this, RobotMainActivity::class.java)
            startActivity(intent)
        }

        // Mostrar el popup al pulsar "Mostrar Gráficos"
        btnMostrarPopup.setOnClickListener {
            popupGraficos.visibility = View.VISIBLE
        }

        // Ocultar el popup al pulsar "Cerrar Pestaña"
        btnCerrarPopup.setOnClickListener {
            popupGraficos.visibility = View.GONE
        }

        // Botón atrás
        btnIrAtras4.setOnClickListener {
            val intent = Intent(this, RobotMainActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("nombre", nombre)
            intent.putExtra("wifi", wifi)
            intent.putExtra("ip", ip)
            startActivity(intent)
        }

        // Carga inicial
        cargarGrafico("imagen_ejecuciones_programa")
    }

    private fun cargarGrafico(endpoint: String) {
        val url = "http://$ipServer:$serverport/$endpoint"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 1000
                connection.readTimeout = 1000
                connection.requestMethod = "HEAD"
                connection.connect()
                println("Conexión exitosa")

                runOnUiThread {
                    Glide.with(this)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .skipMemoryCache(false)
                        .into(imgGrafico)
                }
            } catch (e: Exception) {
                println("No se pudo establecer conexión con el servidor")
                runOnUiThread {
                    Glide.with(this)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .onlyRetrieveFromCache(true)
                        .into(imgGrafico)
                }
            }
        }.start()
    }
}