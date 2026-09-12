package com.example.desafiopractico2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.view.View
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvCatalogo: RecyclerView
    private lateinit var adapter: DestinoAdapter

    private lateinit var btnAgregarDestino: Button

    private lateinit var btnMenu: View


    private val listaDestinos = mutableListOf<DestinoTuristico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = FirebaseDatabase.getInstance().getReference("Destinos")


        rvCatalogo = findViewById(R.id.rvCatalogo)
        rvCatalogo.layoutManager = LinearLayoutManager(this)

        btnMenu = findViewById(R.id.btnMenu)
        btnAgregarDestino = findViewById(R.id.btnAgregarDestino)

        adapter = DestinoAdapter(listaDestinos){
                destinoSeleccionado ->
            val intent = Intent(this, EditarDestinoActivity::class.java)
            intent.putExtra("idDestino",destinoSeleccionado.id)
            startActivity(intent)
        }



        rvCatalogo.adapter = adapter


        cargarDestinos()

        btnAgregarDestino.setOnClickListener {
            val intent = Intent(this, AgregarDestinoActivity::class.java)
            startActivity(intent)
        }

        btnMenu.setOnClickListener { vista ->
            mostrarMenuDesplegable(vista)
        }

    }

    private fun mostrarMenuDesplegable(vista: View) {
        val popup = PopupMenu(this, vista)
        popup.menuInflater.inflate(R.menu.menu_opciones, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.AgregarDestino -> {
                    AgregarDestinoPopUp()
                    true
                }
                R.id.menuCerrarSesion -> {
                    cerrarSesion()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()


        val intent = Intent(this, IniciarSeccionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun AgregarDestinoPopUp(){
        val intent = Intent(this, AgregarDestinoActivity::class.java)
        startActivity(intent)
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

                if (listaTemporal.isEmpty()) {
                    btnAgregarDestino.visibility = View.VISIBLE
                } else {
                    btnAgregarDestino.visibility = View.GONE
                }
                adapter.actualizarLista(listaTemporal)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Error al cargar destinos: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}