package com.example.taller3comp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegistrarActivity : AppCompatActivity() {
    private val CODIGO_SELECCIONAR_IMAGEN = 1
    private val CODIGO_TOMAR_FOTO = 2


    private lateinit var foto: ImageView
    private lateinit var gal: Button
    private lateinit var cam: Button
    private lateinit var cancelar: Button
    private lateinit var enviar: Button
    private lateinit var email: EditText
    private lateinit var nombre: EditText
    lateinit var pasword: EditText
    private lateinit var apellido: EditText
    private lateinit var cedula: EditText
    private lateinit var fotoPerf: ImageView
    var lon: Double = 0.0
    var lat: Double = 0.0
    var fotoAsig: Boolean = false


    private lateinit var rutaImagen: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)


        gal = findViewById<Button>(R.id.cargarImg)
        cam = findViewById<Button>(R.id.foto)
        foto = findViewById<ImageView>(R.id.fotoPerf)
        cancelar = findViewById<Button>(R.id.cancelar)
        enviar = findViewById<Button>(R.id.enviarReg)

        email=findViewById<EditText>(R.id.email)
        nombre=findViewById<EditText>(R.id.nombre)
        pasword=findViewById<EditText>(R.id.pasword)
        apellido=findViewById<EditText>(R.id.apellido)
        cedula=findViewById<EditText>(R.id.cedula)


        gal.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, CODIGO_SELECCIONAR_IMAGEN)
        }
        cam.setOnClickListener {
            //tomarFoto()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            rutaImagen = crearRutaImagen()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, rutaImagen)
            startActivityForResult(intent, CODIGO_TOMAR_FOTO)
        }

        cancelar.setOnClickListener {
            val Intent = Intent(this, AuthActivity::class.java)
            startActivity(Intent)
        }
        enviar.setOnClickListener {
            //falta enviar la informacion que no sea la contraseña y usuario
            if ( email.text.isNotEmpty()&&
                nombre.text.isNotEmpty()&&
                pasword.text.isNotEmpty()&&
                apellido.text.isNotEmpty()&&
                cedula.text.isNotEmpty()&&
                fotoAsig==true )
            {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email.text.toString(),pasword.text.toString()).addOnCompleteListener{
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


    }

    private fun mostrarHome(email: String,provider: ProviderType) {
        val homei =Intent(this,HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider",provider.name)
        }
        startActivity(homei)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CODIGO_SELECCIONAR_IMAGEN -> {
                    val imagenSeleccionada: Uri? = data?.data
                    try {
                        // Cargar la imagen en el ImageView
                        foto.setImageURI(imagenSeleccionada)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                CODIGO_TOMAR_FOTO -> {
                    // Cargar la imagen en el ImageView
                    foto.setImageURI(rutaImagen)
                }
            }
            fotoAsig=true
        } else {
            Log.e("TAG", "Error al obtener la imagen")
            Toast.makeText(this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun crearRutaImagen(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val nombreArchivoImagen = "JPEG_" + timeStamp + "_"
        val directorioAlmacenamiento: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val imagen = File.createTempFile(
            nombreArchivoImagen,
            ".jpg",
            directorioAlmacenamiento
        )
        return FileProvider.getUriForFile(
            this,
            "com.example.taller3comp.fileprovider",
            imagen
        )
    }

    private fun guardarImagenEnGaleria(bitmap: Bitmap) {
        val archivo = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "nombre_imagen.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, archivo)!!

        val stream = contentResolver.openOutputStream(uri)!!
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()

        Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
    }
}