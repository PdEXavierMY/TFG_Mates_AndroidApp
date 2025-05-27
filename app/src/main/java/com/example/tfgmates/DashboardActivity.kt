package com.example.tfgmates

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class DashboardActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)
        enableEdgeToEdge()

        val id = intent.getLongExtra("id", -1L)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val wifi = intent.getStringExtra("wifi") ?: ""
        val ip = intent.getStringExtra("ip") ?: ""

        val popupGraficos = findViewById<LinearLayout>(R.id.popupGraficos)
        val btnMostrarPopup = findViewById<Button>(R.id.ListarGraficas)
        val btnCerrarPopup = findViewById<Button>(R.id.btnCerrar3)

        // Mostrar el popup al pulsar "Mostrar Gráficos"
        btnMostrarPopup.setOnClickListener {
            popupGraficos.visibility = View.VISIBLE
        }

        // Ocultar el popup al pulsar "Cerrar Pestaña"
        btnCerrarPopup.setOnClickListener {
            popupGraficos.visibility = View.GONE
        }
    }
}