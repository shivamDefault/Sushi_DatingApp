package com.example.sushi_datingapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sushi_datingapp.MainActivity
import com.example.sushi_datingapp.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class BirthDateActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birth_date)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users")

        val monthPicker: NumberPicker = findViewById(R.id.monthPicker)
        val dayPicker: NumberPicker = findViewById(R.id.dayPicker)
        val yearPicker: NumberPicker = findViewById(R.id.yearPicker)
        val ageText: TextView = findViewById(R.id.ageText)
        val continueButton: MaterialButton = findViewById(R.id.continueButton)

        // Set up the number pickers
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        dayPicker.minValue = 1
        dayPicker.maxValue = 31
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2023

        // Function to update age text based on selected birth date
        val updateAge = {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val birthYear = yearPicker.value
            val age = currentYear - birthYear
            ageText.text = "$age Years"
        }

        // Update age text initially
        updateAge()

        // Add listeners to update age text when picker values change
        monthPicker.setOnValueChangedListener { _, _, _ -> updateAge() }
        dayPicker.setOnValueChangedListener { _, _, _ -> updateAge() }
        yearPicker.setOnValueChangedListener { _, _, _ -> updateAge() }

        // Continue button click listener
        continueButton.setOnClickListener {
            // Save birth date and age to Firebase Realtime Database
            val birthDate = "${monthPicker.value}/${dayPicker.value}/${yearPicker.value}"
            val age = ageText.text.toString() // Get age as string

            val currentUser = auth.currentUser
            currentUser?.phoneNumber?.let { phoneNumber ->
                val userRef = database.child(phoneNumber)
                userRef.child("birthdate").setValue(birthDate)
                    .addOnSuccessListener {
                        // Store age as well
                        userRef.child("age").setValue(age)
                            .addOnSuccessListener {
                                // Navigate to MainActivity upon successful save
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                // Handle failure to save age to database
                                e.printStackTrace()
                            }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure to save birthdate to database
                        e.printStackTrace()
                    }
            }
        }
    }
}
