package com.example.desafiopractico2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CatalogoActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvCatalogo: RecyclerView
    private lateinit var adapter: DestinoAdapter

    private val listaDestinos = mutableListOf<DestinoTuristico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)


        database = FirebaseDatabase.getInstance().getReference("Destinos")


        rvCatalogo = findViewById(R.id.rvCatalogo)
        rvCatalogo.layoutManager = LinearLayoutManager(this)

        adapter = DestinoAdapter(listaDestinos){
            destinoSeleccionado ->
            val intent = Intent(this, EditarDestinoActivity::class.java)
            intent.putExtra("idDestino",destinoSeleccionado.id)
            startActivity(intent)
        }

        rvCatalogo.adapter = adapter


        cargarDestinos()
    }

    private fun cargarDestinos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaTemporal = mutableListOf<DestinoTuristico>()

                for (hijo in snapshot.children) {
                    val destino = hijo.getValue(DestinoTuristico::class.java)
                    if (destino != null) {
                        listaTemporal.add(destino)
                    }
                }

                adapter.actualizarLista(listaTemporal)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@CatalogoActivity,
                    "Error al cargar destinos: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}