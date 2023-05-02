package com.example.taller3comp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType {
    BASIC
}
class HomeActivity : AppCompatActivity() {
    private lateinit var viewEmail: TextView
    private lateinit var viewProvider: TextView
    private lateinit var cerrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewEmail=findViewById<TextView>(R.id.viewEmail)

        viewProvider=findViewById<TextView>(R.id.viewProvider)

        cerrar=findViewById<Button>(R.id.cerrar)

        var bundle=intent.extras
        var email= bundle?.getString("email")
        var provider= bundle?.getString("provider")
        setup(email ?:"", provider?: "")

        //GUARDAR
        val prefs=getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()
    }
    fun setup(email: String, provider: String)
    {
        viewEmail.text=email
        viewProvider.text=provider

        cerrar.setOnClickListener()
        {

            val prefs=getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }




    }

}