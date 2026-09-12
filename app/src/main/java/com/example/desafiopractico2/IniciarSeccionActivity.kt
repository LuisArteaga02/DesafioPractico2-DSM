package com.example.desafiopractico2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.desafiopractico2.RegistroActivity
import com.google.firebase.auth.FirebaseAuth

class IniciarSeccionActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnIniciarSeccion: Button

    private lateinit var btnRegistro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_seccion)


        auth = FirebaseAuth.getInstance()


        etEmail = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnIniciarSeccion = findViewById(R.id.btnIniciarSeccion)
        btnRegistro = findViewById(R.id.btnRegistrar)


        btnIniciarSeccion.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()


            if (email.isEmpty() || password.isEmpty()) {
                mostrarMensaje("Debe ingresar su correo y contraseña.")
                return@setOnClickListener
            }



            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        mostrarMensaje("¡Bienvenido!")



                        val intent = Intent(this, MainActivity::class.java)
                         startActivity(intent)
                         finish()
                    } else {

                        mostrarMensaje("Error al iniciar sesión: ${task.exception?.message}")
                    }
                }
        }

        btnRegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}