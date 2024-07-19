package com.example.sushi_datingapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sushi_datingapp.R
import com.example.sushi_datingapp.databinding.ActivityStatusBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class StatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatusBinding
    private var selectedStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStatusSelection()

        binding.continueButton.setOnClickListener {
            if (selectedStatus.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show()
            } else {
                // Save selected status to Firebase
                saveStatusToFirebase(selectedStatus!!)
            }
        }
    }

    private fun setupStatusSelection() {
        binding.relationshipButton.setOnClickListener {
            selectStatus("relationship")
        }

        binding.casualButton.setOnClickListener {
            selectStatus("casual")
        }

        binding.notSureButton.setOnClickListener {
            selectStatus("not_sure_yet")
        }
    }

    private fun selectStatus(status: String) {
        selectedStatus = status

        // Reset background colors
        binding.relationshipButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.casualButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.notSureButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

        // Apply visual feedback to the selected status
        when (status) {
            "relationship" -> binding.relationshipButton.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.green
                )
            )

            "casual" -> binding.casualButton.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.green
                )
            )

            "not_sure_yet" -> binding.notSureButton.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.green
                )
            )
        }
    }

    private fun saveStatusToFirebase(status: String) {
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val databaseReference =
            FirebaseDatabase.getInstance().getReference("users").child(phoneNumber)
        databaseReference.child("relationship").setValue(status).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Status saved successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SignActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
