package com.example.tfgmates

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.lifecycle.lifecycleScope
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tfgmates.bbdd.AppDatabase
import com.example.tfgmates.bbdd.dao.WiFiDao
import com.example.tfgmates.bbdd.entities.WiFiRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var popupLayout: View
    private lateinit var listaRobots: ListView
    private lateinit var btnCerrar: Button
    private lateinit var btnRefrescar: Button
    private lateinit var btnNuevaConexion: Button
    private lateinit var progressBar: ProgressBar  // ProgressBar para mostrar la carga
    private lateinit var db: AppDatabase
    private lateinit var contenedorRobots: LinearLayout
    private lateinit var wifiDao: WiFiDao

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val results: List<ScanResult> = wifiManager.scanResults
                val robots = results.filter { it.SSID.lowercase().contains("niryo") }
                    .map { it.SSID }

                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, robots)
                listaRobots.adapter = adapter
            } else {
                Toast.makeText(this@MainActivity, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
            }

            progressBar.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        // Inicializar base de datos utilizando el patrón Singleton
        db = AppDatabase.getInstance(this)  // Usamos el patrón Singleton
        wifiDao = db.wifiDao()

        popupLayout = findViewById(R.id.popupLayout)
        listaRobots = findViewById(R.id.listaRobots)
        btnCerrar = findViewById(R.id.btnCerrar)
        btnRefrescar = findViewById(R.id.btnRefrescar)
        btnNuevaConexion = findViewById(R.id.buttonNuevaConexion)
        progressBar = findViewById(R.id.progressBar)
        contenedorRobots = findViewById(R.id.contenedorRobots)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        cargarRobots()

        btnNuevaConexion.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                popupLayout.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                wifiManager.startScan()
            } else {
                Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
            }
        }

        btnCerrar.setOnClickListener {
            popupLayout.visibility = View.GONE
            cargarRobots()
        }

        btnRefrescar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                progressBar.visibility = View.VISIBLE
                wifiManager.startScan()
            } else {
                Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
            }
        }

        listaRobots.setOnItemClickListener { _, _, position, _ ->
            val wifiName = listaRobots.getItemAtPosition(position).toString()
            insertarYMostrarRobot(wifiName)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiScanReceiver)
    }

    // Manejo de la respuesta a la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertarYMostrarRobot(wifiName: String) {
        lifecycleScope.launch {
            val robot = withContext(Dispatchers.IO) {
                val dao = db.wifiDao()
                var registro = dao.getWiFiByName(wifiName)

                if (registro == null) {
                    val nuevo = WiFiRecord(nombre = "", wifi = wifiName, ip = "")
                    dao.insert(nuevo)
                    registro = dao.getWiFiByName(wifiName) // Volver a leerlo con su ID
                }

                registro
            }

            robot?.let {
                val intent = Intent(this@MainActivity, RobotMainActivity::class.java).apply {
                    putExtra("id", it.id)
                    putExtra("nombre", it.nombre)
                    putExtra("wifi", it.wifi)
                    putExtra("ip", it.ip)
                }
                startActivity(intent)
            }
        }
    }

    private fun cargarRobots() {
        lifecycleScope.launch {
            val robots = withContext(Dispatchers.IO) { wifiDao.getAllWiFis() }

            contenedorRobots.removeAllViews() // Limpiar antes de agregar

            var rowLayout: LinearLayout? = null  // Layout horizontal actual
            for ((index, robot) in robots.withIndex()) {
                if (index % 3 == 0) { // Cada 3 robots, crear nueva fila
                    rowLayout = LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 20, 0, 20) // Margen entre filas
                        }
                        orientation = LinearLayout.HORIZONTAL
                    }
                    contenedorRobots.addView(rowLayout) // Agregar fila al contenedor
                }

                val layoutItem = LinearLayout(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,  // Distribuir espacio equitativamente
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        if (index % 3 == 0) setMargins(60, 0, 0, 0) // Margen al primer robot de la fila
                    }
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                }

                val imageView = ImageView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(200, 200)
                    setImageResource(R.drawable.robot)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }

                val textView = TextView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = if (robot.nombre.isNotEmpty()) robot.nombre else robot.wifi
                    textSize = 18f
                    gravity = Gravity.CENTER
                }

                // Evento al hacer clic en un robot
                layoutItem.setOnClickListener {
                    val intent = Intent(this@MainActivity, RobotMainActivity::class.java).apply {
                        putExtra("id", robot.id)
                        putExtra("nombre", robot.nombre)
                        putExtra("wifi", robot.wifi)
                        putExtra("ip", robot.ip)
                    }
                    startActivity(intent)
                }

                layoutItem.addView(imageView)
                layoutItem.addView(textView)
                rowLayout?.addView(layoutItem) // Agregar item a la fila actual
            }
        }
    }
}