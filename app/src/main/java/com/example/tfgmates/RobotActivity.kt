package com.example.tfgmates

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class RobotActivity: AppCompatActivity() {

    private lateinit var btnIrAtras2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.robot)
        enableEdgeToEdge()

        // Obtener los datos del Intent
        val id = intent.getLongExtra("id", -1L)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val wifi = intent.getStringExtra("wifi") ?: ""
        val ip = intent.getStringExtra("ip") ?: ""

        btnIrAtras2 = findViewById(R.id.buttonAtras2)
        btnIrAtras2.setOnClickListener {
            val intent = Intent(this, RobotMainActivity::class.java)

            intent.putExtra("id", id)
            intent.putExtra("nombre", nombre)
            intent.putExtra("wifi", wifi)
            intent.putExtra("ip", ip)

            startActivity(intent)
        }
    }
}