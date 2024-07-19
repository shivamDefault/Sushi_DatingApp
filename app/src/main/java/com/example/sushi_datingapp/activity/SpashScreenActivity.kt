package com.example.sushi_datingapp.activity



import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sushi_datingapp.MainActivity
import com.example.sushi_datingapp.R
import com.example.sushi_datingapp.auth.LoginActivity


import com.google.firebase.auth.FirebaseAuth

class SpashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_spash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val user = FirebaseAuth.getInstance().currentUser
        Handler(Looper.getMainLooper()).postDelayed({
            if (user == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            else{
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 2000)
    }
}