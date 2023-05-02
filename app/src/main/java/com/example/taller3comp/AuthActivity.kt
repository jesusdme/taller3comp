package com.example.taller3comp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val PERMISSION_REQUEST_CAMERA = 1 // Identificador para la solicitud de permiso de la cámara
    private val REQUEST_IMAGE_CAPTURE = 2 // Identificador para la solicitud de captura de imagen
    private val REQUEST_WRITE_EXTERNAL_STORAGE =3 // Identificador para guardar la imagen
    private val REQUEST_READ_EXTERNAL_STORAGE =6 // Identificador para guardar la imagen

    private val PERMISSIONS_REQUEST_READ_CONTACTS = 4
    private val PERMISSIONS_REQUEST_ACCESS_LOCATION = 5 //id localizacion
    private val REQUEST_CODE = 101
    private lateinit var passwordLog:EditText
    private lateinit var emailLog:EditText
    private lateinit var authL:ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        //auth= Firebase.auth
        val acced = findViewById<Button>(R.id.acceder)
        val reg = findViewById<Button>(R.id.registro)

        emailLog= findViewById<EditText>(R.id.emailLog)
        passwordLog= findViewById<EditText>(R.id.passwordLog)
        authL=findViewById<ConstraintLayout>(R.id.authL)

        reg.setOnClickListener {
            // Solicitar permiso para usar la cámara si aún no se ha concedido
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
            } else {
                // El permiso ya ha sido concedido
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_WRITE_EXTERNAL_STORAGE)
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_READ_EXTERNAL_STORAGE)
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                ),
                                PERMISSIONS_REQUEST_ACCESS_LOCATION
                            )

                        } else //si tiene permiso usar la ubicacion
                        {
                            // El permiso ya ha sido concedido
                            val Intent = Intent(this, RegistrarActivity::class.java)
                            startActivity(Intent)
                        }
                    }
                }
            }
        }



        acced.setOnClickListener {
            //
            if ( emailLog.text.isNotEmpty()&&
                passwordLog.text.isNotEmpty())
            {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(emailLog.text.toString(),passwordLog.text.toString()).addOnCompleteListener{
                        if (it.isSuccessful){
                            mostrarHome(it.result?.user?.email?: "",ProviderType.BASIC)
                        }
                        else
                            Toast.makeText(this, "No se pudo completar el registro", Toast.LENGTH_SHORT).show()
                    }

                val Intent = Intent(this, MapsActivity::class.java)
                startActivity(Intent)
            }
        }

        sesion()
    }

    override fun onStart() {
        super.onStart()
        authL.visibility= View.VISIBLE
    }
    private fun sesion(){

        val prefs=getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email =prefs.getString("email",null)
        val provider=prefs.getString("provider",null)
        if(email!= null && provider!= null)
        {
            authL.visibility= View.INVISIBLE
            mostrarHome(email, ProviderType.valueOf(provider))
        }

    }

    private fun mostrarHome(email: String,provider: ProviderType) {
        val homei =Intent(this,HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider",provider.name)
        }
        startActivity(homei)

    }

//
//    override fun onStart() {
//        super.onStart()
//        //val currentUser=auth.currentUser
//        //updateUI(currentUser)
//    }



}

