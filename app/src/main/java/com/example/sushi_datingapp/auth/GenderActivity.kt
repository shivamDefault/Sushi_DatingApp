package com.example.sushi_datingapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sushi_datingapp.R
import com.example.sushi_datingapp.databinding.ActivityGenderBinding
import com.example.sushi_datingapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GenderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenderBinding
    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGenderSelection()

        binding.btnContinue.setOnClickListener {
            if (selectedGender.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            } else {
                saveGenderToFirebase(selectedGender!!)
            }
        }
    }

    private fun setupGenderSelection() {
        binding.maleLayout.setOnClickListener {
            selectGender("Male")
        }

        binding.femaleLayout.setOnClickListener {
            selectGender("Female")
        }
    }

    private fun selectGender(gender: String) {
        selectedGender = gender

        // Reset background colors
        binding.maleLayout.setBackgroundResource(R.drawable.border_background)
        binding.femaleLayout.setBackgroundResource(R.drawable.border_background)

        // Apply visual feedback to the selected gender
        when (gender) {
            "Male" -> binding.maleLayout.setBackgroundResource(R.drawable.selected_border_background)
            "Female" -> binding.femaleLayout.setBackgroundResource(R.drawable.selected_border_background)
        }
    }

    private fun saveGenderToFirebase(gender: String) {
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val databaseReference =
            FirebaseDatabase.getInstance().getReference("users").child(phoneNumber)
        databaseReference.child("gender").setValue(gender).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Gender saved successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, StatusActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
