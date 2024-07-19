package com.example.sushi_datingapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.sushi_datingapp.MainActivity
import com.example.sushi_datingapp.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users")

        val continueButton: MaterialButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener {
            // Navigate to BirthDateActivity
            val intent = Intent(this, BirthDateActivity::class.java)
            startActivity(intent)
        }

        // Handle clicks on the star sign grid items
        setupStarSignClickListeners()
    }

    private fun setupStarSignClickListeners() {
        val starSigns = listOf(
            R.id.capricorn_layout, R.id.taurus_layout, R.id.leo_layout, R.id.libra_layout,
            R.id.aquarius_layout, R.id.cancer_layout, R.id.sagittarius_layout, R.id.gemini_layout,
            R.id.pisces_layout, R.id.scorpio_layout, R.id.virgo_layout, R.id.aries_layout
        )

        for (sign in starSigns) {
            val signLayout: LinearLayout = findViewById(sign)
            signLayout.setOnClickListener {
                handleStarSignClick(sign)
            }
        }
    }

    private fun handleStarSignClick(selectedSignId: Int) {
        // Reset all background colors
        val starSignLayouts = listOf(
            R.id.capricorn_layout, R.id.taurus_layout, R.id.leo_layout, R.id.libra_layout,
            R.id.aquarius_layout, R.id.cancer_layout, R.id.sagittarius_layout, R.id.gemini_layout,
            R.id.pisces_layout, R.id.scorpio_layout, R.id.virgo_layout, R.id.aries_layout
        )

        for (layoutId in starSignLayouts) {
            val starSignLayout: LinearLayout = findViewById(layoutId)
            starSignLayout.setBackgroundResource(R.drawable.border_background)
        }

        // Highlight the selected star sign
        val selectedLayout: LinearLayout = findViewById(selectedSignId)
        selectedLayout.setBackgroundResource(R.drawable.selected_border_background)

        // Get the star sign text based on the selected layout
        val selectedStarSign = when (selectedSignId) {
            R.id.capricorn_layout -> "Capricorn"
            R.id.taurus_layout -> "Taurus"
            R.id.leo_layout -> "Leo"
            R.id.libra_layout -> "Libra"
            R.id.aquarius_layout -> "Aquarius"
            R.id.cancer_layout -> "Cancer"
            R.id.sagittarius_layout -> "Sagittarius"
            R.id.gemini_layout -> "Gemini"
            R.id.pisces_layout -> "Pisces"
            R.id.scorpio_layout -> "Scorpio"
            R.id.virgo_layout -> "Virgo"
            R.id.aries_layout -> "Aries"
            else -> ""
        }

        // Save selected star sign to Firebase Realtime Database under user's phone number
        val currentUser = auth.currentUser
        currentUser?.phoneNumber?.let { phoneNumber ->
            val userRef = database.child(phoneNumber)
            userRef.child("zodiac").setValue(selectedStarSign)
                .addOnSuccessListener {
                    // Successfully saved, do any additional operations if needed
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    e.printStackTrace()
                }
        }
    }
}
