package com.example.tfgmates

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.transition.Visibility
import com.example.tfgmates.helpers.HttpHelper
import com.example.tfgmates.helpers.HttpHelper.sendHttpRequest
import com.example.tfgmates.helpers.InternetHelper
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ReportActivity : AppCompatActivity() {

    private lateinit var micLayout: LinearLayout
    private lateinit var micIcon: ImageView
    private lateinit var micText: TextView
    private lateinit var imageView: ImageView
    private lateinit var transcriptText: TextView
    private lateinit var btnGenerarReporte: Button
    private lateinit var btnTomarFoto: Button
    private lateinit var btnRepetirIncidencia: Button
    private lateinit var btnEnviarIncidencia: Button
    private lateinit var popUpConfirmacion: LinearLayout

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var isListening = false

    companion object {
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }

    private val REQUEST_ID = 123

    private val cameraLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val photo = result.data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                imageView.setImageBitmap(photo)
            } else {
                Log.e("Camera", "No se pudo obtener la imagen completa")
            }
        } else {
            Log.e("Camera", "No se obtuvo RESULT_OK")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.report_main)
        enableEdgeToEdge()

        micLayout = findViewById(R.id.linearLayoutMicro)
        micIcon = findViewById(R.id.imageViewMic)
        micText = findViewById(R.id.textViewText)
        transcriptText = findViewById(R.id.textoReport)
        btnGenerarReporte = findViewById(R.id.generarReporte)
        btnTomarFoto = findViewById(R.id.tomarFoto)
        imageView = findViewById(R.id.imagenCapturada)
        popUpConfirmacion = findViewById(R.id.popupInforme)
        btnRepetirIncidencia = findViewById(R.id.btnRepetirInforme)
        btnEnviarIncidencia = findViewById(R.id.btnEnviarInforme)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            initModel()
        }

        micLayout.setOnClickListener {
            if (isListening) {
                stopListening()
            } else {
                startListening()
            }
        }

        btnGenerarReporte.setOnClickListener {
            generarInforme()
        }

        btnTomarFoto.setOnClickListener {
            launchCameraRawPhoto()
        }

        btnRepetirIncidencia.setOnClickListener {
            transcriptText.text = ""
            popUpConfirmacion.visibility = View.GONE
        }

        btnEnviarIncidencia.setOnClickListener {
            enviarReporte()
        }
    }

    private fun enviarReporte() {

    }

    private fun launchCameraRawPhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun generarInforme() {
        val flowUrl = "https://prod-38.westus.logic.azure.com:443/workflows/6cbcc847b99c4ed4ba43ce6201d67b09/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=I6Wp8Xxe5_kVHZ1mzkBb3c5L4FtAclJTIhwdeypIXyY"

        val nuevoMicTexto = transcriptText.text.toString()

        if (InternetHelper.hayConexionInternet(this)) {
            val jsonBody = JSONObject().apply {
                put("industria", "Logística")
                put("refinar", false)
                put("micTexto", nuevoMicTexto)
            }

            sendHttpRequest(flowUrl, jsonBody) { response ->
                runOnUiThread {
                    if (response != null) {
                        transcriptText.text = response.ifEmpty {
                            "No se recibió ninguna incidencia."
                        }
                        popUpConfirmacion.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Error en la solicitud HTTP", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Se requiere conexión a Internet para formalizar la incidencia", Toast.LENGTH_LONG).show()
        }
    }

    private fun initModel() {
        Log.d("Vosk", "Cargando modelo desde assets...")

        // Ruta del modelo en assets
        val modelPath = "vosk-model-small-es-0.42" // El nombre de la carpeta del modelo dentro de assets

        // Directorio temporal donde se extraerán los archivos del modelo
        val tempDir = File(applicationContext.cacheDir, "vosk_model").apply {
            if (!exists()) {
                mkdirs() // Crea el directorio si no existe
            }
        }

        // Intentamos copiar los archivos de assets a la carpeta temporal
        try {
            val assetManager = assets
            copyAssets(assetManager, modelPath, tempDir) // Copia los archivos de assets de manera recursiva

            // Ahora que el modelo está copiado a un directorio accesible, lo cargamos
            model = Model(tempDir.absolutePath)
            Log.d("Vosk", "Modelo cargado correctamente desde: ${tempDir.absolutePath}")
        } catch (e: Exception) {
            Log.e("Vosk", "Error cargando el modelo: ${e.message}")
        }
    }

    private fun copyAssets(assetManager: AssetManager, sourceDir: String, destDir: File) {
        val files = assetManager.list(sourceDir)
        if (files != null) {
            for (file in files) {
                val srcPath = "$sourceDir/$file"
                val destFile = File(destDir, file)

                // Si es una subcarpeta, recursivamente copiar su contenido
                if (assetManager.list(srcPath).isNullOrEmpty()) {
                    // Si no es una subcarpeta, copiar el archivo
                    val inputStream = assetManager.open(srcPath)
                    val outputStream = FileOutputStream(destFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                } else {
                    // Si es una subcarpeta, crear el directorio y llamar recursivamente
                    destFile.mkdirs()
                    copyAssets(assetManager, srcPath, destFile)
                }
            }
        }
    }

    private fun startListening() {
        val model = model
        if (model == null) {
            Toast.makeText(this, "El modelo aún se está cargando...", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {
                }

                override fun onResult(hypothesis: String?) {
                }

                override fun onFinalResult(hypothesis: String?) {
                    runOnUiThread {
                        if (!hypothesis.isNullOrEmpty()) {
                            try {
                                val jsonObject = JSONObject(hypothesis)
                                val texto = jsonObject.optString("text", "")

                                if (texto.isNotEmpty()) {
                                    transcriptText.text = texto
                                } else {
                                    Log.e("Vosk", "No se encontró la clave 'text' en la transcripción.")
                                }
                            } catch (e: Exception) {
                                Log.e("Vosk", "Error al procesar el JSON: ${e.message}")
                            }
                        }
                    }
                }

                override fun onError(e: Exception?) {
                    Log.e("VOSK_ERROR", "Error: ", e ?: Exception("Error desconocido"))
                }

                override fun onTimeout() {
                    stopListening()
                }
            })

            isListening = true
            micIcon.setImageResource(R.drawable.micro_img2)
            micText.text = "Parar Micro"

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopListening() {
        speechService?.stop()
        speechService?.shutdown()
        isListening = false
        micIcon.setImageResource(R.drawable.micro_img)
        micText.text = "Iniciar Micro"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initModel()
            } else {
                Toast.makeText(this, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}