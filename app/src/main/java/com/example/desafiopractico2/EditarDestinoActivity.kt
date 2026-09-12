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
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import androidx.appcompat.app.AlertDialog

class EditarDestinoActivity : AppCompatActivity() {

    private lateinit var etNombreDestino: EditText
    private lateinit var spinnerPais: Spinner
    private lateinit var etPrecio: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnSeleccionarImagen: Button
    private lateinit var ivVistaPrevia: ImageView
    private lateinit var btnGuardarCambios: Button
    private lateinit var btnEliminarDestino: Button

    private lateinit var database: DatabaseReference
    private var idDestino: String = ""
    private var rutaImagenActual: String = ""
    private var imagenSeleccionadaUri: Uri? = null

    private val opcionesPaises = arrayOf("Seleccione un país", "El Salvador", "Guatemala", "México", "Japón", "Francia")

    private val abrirGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            ivVistaPrevia.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_destino)


        idDestino = intent.getStringExtra("idDestino") ?: ""
        if (idDestino.isEmpty()) {
            Toast.makeText(this, "Error: destino no encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("Destinos")


        etNombreDestino = findViewById(R.id.etNombreDestino)
        spinnerPais = findViewById(R.id.spinnerPais)
        etPrecio = findViewById(R.id.etPrecio)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen)
        ivVistaPrevia = findViewById(R.id.ivVistaPrevia)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
        btnEliminarDestino = findViewById(R.id.btnEliminarDestino)

        val adapterPaises = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesPaises)
        spinnerPais.adapter = adapterPaises


        cargarDatosDestino()

        btnSeleccionarImagen.setOnClickListener {
            abrirGaleria.launch("image/*")
        }

        btnGuardarCambios.setOnClickListener {
            validarYActualizarDatos()
        }

        btnEliminarDestino.setOnClickListener {
            mostrarConfirmacionEliminar()
        }

    }

    private fun mostrarConfirmacionEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar destino")
            .setMessage("¿Estás seguro de que deseas eliminar este destino? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                eliminarDestino()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun eliminarDestino() {
        btnEliminarDestino.isEnabled = false
        btnGuardarCambios.isEnabled = false

        database.child(idDestino).removeValue()
            .addOnSuccessListener {

                eliminarImagenLocal(rutaImagenActual)

                Toast.makeText(this, "Destino eliminado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                mostrarError("Error al eliminar: ${it.message}")
                btnEliminarDestino.isEnabled = true
                btnGuardarCambios.isEnabled = true
            }
    }

    private fun eliminarImagenLocal(ruta: String) {
        try {
            if (ruta.isNotEmpty()) {
                val archivo = File(ruta)
                if (archivo.exists()) {
                    archivo.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun cargarDatosDestino() {
        database.child(idDestino).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val destino = snapshot.getValue(DestinoTuristico::class.java)
                if (destino != null) {
                    etNombreDestino.setText(destino.nombre)
                    etPrecio.setText(destino.precio.toString())
                    etDescripcion.setText(destino.descripcion)
                    rutaImagenActual = destino.imagenUrl


                    val posicionPais = opcionesPaises.indexOf(destino.pais)
                    if (posicionPais >= 0) {
                        spinnerPais.setSelection(posicionPais)
                    }


                    Glide.with(this@EditarDestinoActivity)
                        .load(rutaImagenActual)
                        .centerCrop()
                        .into(ivVistaPrevia)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@EditarDestinoActivity,
                    "Error al cargar datos: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun validarYActualizarDatos() {
        val nombre = etNombreDestino.text.toString().trim()
        val pais = spinnerPais.selectedItem.toString()
        val precioStr = etPrecio.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()


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

        btnGuardarCambios.isEnabled = false
        btnGuardarCambios.text = "Guardando..."


        if (imagenSeleccionadaUri != null) {
            val nuevaRuta = guardarImagenLocalmente(imagenSeleccionadaUri!!)
            if (nuevaRuta != null) {
                actualizarDestinoEnBD(nombre, pais, precio, descripcion, nuevaRuta)
            } else {
                mostrarError("Error al procesar la nueva imagen.")
                restaurarBoton()
            }
        } else {
            actualizarDestinoEnBD(nombre, pais, precio, descripcion, rutaImagenActual)
        }
    }

    private fun guardarImagenLocalmente(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val nombreArchivo = "destino_${System.currentTimeMillis()}.jpg"
            val archivoLocal = File(filesDir, nombreArchivo)
            val outputStream = FileOutputStream(archivoLocal)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            archivoLocal.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun actualizarDestinoEnBD(nombre: String, pais: String, precio: Double, descripcion: String, rutaImagen: String) {
        val destinoActualizado = DestinoTuristico(idDestino, nombre, pais, precio, descripcion, rutaImagen)

        database.child(idDestino).setValue(destinoActualizado)
            .addOnSuccessListener {
                Toast.makeText(this, "Destino actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish() // Vuelve al catálogo, que se actualiza solo gracias al addValueEventListener
            }
            .addOnFailureListener {
                mostrarError("Error al actualizar: ${it.message}")
                restaurarBoton()
            }
    }

    private fun restaurarBoton() {
        btnGuardarCambios.isEnabled = true
        btnGuardarCambios.text = "Guardar Cambios"
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}