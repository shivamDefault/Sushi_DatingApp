package com.example.sushi_datingapp.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sushi_datingapp.MainActivity
import com.example.sushi_datingapp.R
import com.example.sushi_datingapp.databinding.ActivityRegistrationBinding
import com.example.sushi_datingapp.model.UserModel
import com.example.sushi_datingapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private var imageUri: Uri? = null
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.userImage.setImageURI(imageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.userImage.setOnClickListener { selectImage.launch("image/*") }
        binding.saveData.setOnClickListener { validateData() }
    }

    private fun validateData() {
        if (binding.userName.text.toString().isEmpty() || binding.userEmail.text.toString()
                .isEmpty() || binding.userLocation.text.toString().isEmpty() || imageUri == null
        ) {
            Toast.makeText(this, "Fields should not be empty", Toast.LENGTH_SHORT).show()
        } else if (!binding.termsCondition.isChecked) {
            Toast.makeText(this, "Please accept our terms and conditions", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage()
        }
    }

    private fun uploadImage() {
        // Show progress dialog
        Config.showDialog(this)

        // Storage reference
        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("profile.jpg")

        // Upload image to Firebase Storage
        storageRef.putFile(imageUri!!).addOnSuccessListener {
            // Once image is uploaded, get download URL
            storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                // Store user data in Firebase Realtime Database
                storeUserData(imageUrl)
            }.addOnFailureListener { e ->
                // Handle failure to get download URL
                Config.hideDialog()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            // Handle failure to upload image
            Config.hideDialog()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeUserData(imageUrl: Uri?) {
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val data = UserModel(
            name = binding.userName.text.toString(),
            email = binding.userEmail.text.toString(),
            city = binding.userLocation.text.toString(),
            number = phoneNumber,
            image = imageUrl.toString()
        )
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Save user data under phone number in Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("users").child(phoneNumber ?: "").setValue(data)
            .addOnCompleteListener { task ->
                Config.hideDialog()
                if (task.isSuccessful) {
                    startActivity(Intent(this, GenderActivity::class.java))
                    finish()
                    Toast.makeText(this, "Successfully registered", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
