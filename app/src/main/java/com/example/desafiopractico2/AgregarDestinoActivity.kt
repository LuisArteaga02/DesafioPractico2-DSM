package com.example.desafiopractico2

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AgregarDestinoActivity : AppCompatActivity() {

    private lateinit var etNombreDestino: EditText
    private lateinit var spinnerPais: Spinner
    private lateinit var etPrecio: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnSeleccionarImagen: Button
    private lateinit var ivVistaPrevia: ImageView
    private lateinit var btnGuardarDestino: Button

    private lateinit var database: DatabaseReference
    private var imagenSeleccionadaUri: Uri? = null

    // API moderna para abrir la galería
    private val abrirGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            ivVistaPrevia.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_destino)

        // Inicializar Firebase (Solo Realtime Database)
        database = FirebaseDatabase.getInstance().getReference("Destinos")

        // Enlazar vistas
        etNombreDestino = findViewById(R.id.etNombreDestino)
        spinnerPais = findViewById(R.id.spinnerPais)
        etPrecio = findViewById(R.id.etPrecio)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen)
        ivVistaPrevia = findViewById(R.id.ivVistaPrevia)
        btnGuardarDestino = findViewById(R.id.btnGuardarDestino)

        val opcionesPaises = arrayOf("Seleccione un país", "El Salvador", "Guatemala", "México", "Japón", "Francia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesPaises)
        spinnerPais.adapter = adapter

        btnSeleccionarImagen.setOnClickListener {
            abrirGaleria.launch("image/*")
        }

        btnGuardarDestino.setOnClickListener {
            validarYGuardarDatos()
        }
    }

    private fun validarYGuardarDatos() {
        val nombre = etNombreDestino.text.toString().trim()
        val pais = spinnerPais.selectedItem.toString()
        val precioStr = etPrecio.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()

        // Validaciones obligatorias
        if (nombre.isEmpty() || pais == "Seleccione un país" || precioStr.isEmpty() || descripcion.isEmpty()) {
            mostrarError("Ningún campo puede estar vacío.")
            return
        }

        val precio = precioStr.toDoubleOrNull()
        if (precio == null || precio <= 0) {
            mostrarError("El precio debe ser un número válido mayor a 0.")
            return
        }

        if (descripcion.length < 20) {
            mostrarError("La descripción debe tener al menos 20 caracteres.")
            return
        }

        if (imagenSeleccionadaUri == null) {
            mostrarError("Debe seleccionar una imagen obligatoriamente.")
            return
        }


        val rutaImagenLocal = guardarImagenLocalmente(imagenSeleccionadaUri!!)

        if (rutaImagenLocal != null) {

            guardarDestinoEnBD(nombre, pais, precio, descripcion, rutaImagenLocal)
        } else {
            mostrarError("Error al procesar la imagen localmente.")
        }
    }


    private fun guardarImagenLocalmente(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)

            val nombreArchivo = "destino_${System.currentTimeMillis()}.jpg"
            val archivoLocal = File(filesDir, nombreArchivo)
            val outputStream = FileOutputStream(archivoLocal)

            // Copiamos los bytes
            inputStream?.copyTo(outputStream)


            inputStream?.close()
            outputStream.close()


            archivoLocal.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun guardarDestinoEnBD(nombre: String, pais: String, precio: Double, descripcion: String, rutaImagen: String) {
        val idDestino = database.push().key ?: UUID.randomUUID().toString()


        val nuevoDestino = DestinoTuristico(idDestino, nombre, pais, precio, descripcion, rutaImagen)

        database.child(idDestino).setValue(nuevoDestino)
            .addOnSuccessListener {
                Toast.makeText(this, "Destino guardado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                mostrarError("Error al guardar en la base de datos.")
            }
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}