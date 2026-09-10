import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.desafiopractico2.AgenteViajes
import com.example.desafiopractico2.IniciarSeccionActivity
import com.example.desafiopractico2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistroActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegistrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Usuarios")


        etNombre = findViewById(R.id.etnombre)
        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnRegistrar = findViewById(R.id.btnRegistrar)


        btnRegistrar.setOnClickListener {

            val nombre = etNombre.text.toString().trim()
            val email = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()


            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                mostrarError("Todos los campos son obligatorios.")
                return@setOnClickListener
            }


            if (password.length < 6) {
                mostrarError("La contraseña debe tener al menos 6 caracteres.")
                return@setOnClickListener
            }


            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val userId = auth.currentUser?.uid ?: ""

                        val nuevoAgente = AgenteViajes(userId, nombre, email)

                        database.child(userId).setValue(nuevoAgente)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                                 startActivity(Intent(this, IniciarSeccionActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { error ->
                                mostrarError("Error en base de datos: ${error.message}")
                            }
                    } else {

                        mostrarError(task.exception?.message ?: "Error en el registro")
                    }
                }
        }
    }

    
    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}