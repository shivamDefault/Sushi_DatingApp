package com.example.sushi_datingapp.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sushi_datingapp.MainActivity
import com.example.sushi_datingapp.R
import com.example.sushi_datingapp.databinding.ActivityEditProfileBinding
import com.example.sushi_datingapp.model.UserModel
import com.example.sushi_datingapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.userImage.setImageURI(imageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference("users").child(currentUser.phoneNumber!!)
                .get().addOnSuccessListener {
                    if (it.exists()) {
                        val data = it.getValue(UserModel::class.java)
                        binding.userName.setText(data?.name)
                        binding.userEmail.setText(data?.email)
                        binding.userNumber.setText(data?.number)
                        binding.userCity.setText(data?.city)
                        binding.userCity.setText(data?.city)
                        binding.userAge.setText(data?.age)
                        Glide.with(this).load(data?.image).placeholder(R.drawable.man)
                            .into(binding.userImage)
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }

        binding.userImage.setOnClickListener { selectImage.launch("image/*") }
        binding.saveData.setOnClickListener { validateAndSaveData() }
    }

    private fun validateAndSaveData() {
        if (binding.userName.text.toString().isEmpty() || binding.userEmail.text.toString()
                .isEmpty() || binding.userNumber.text.toString()
                .isEmpty() || binding.userCity.text.toString().isEmpty()
        ) {
            Toast.makeText(this, "Fields should not be empty", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri != null) {
                uploadImage()
            } else {
                updateProfile(null)
            }
        }
    }

    private fun uploadImage() {
        Config.showDialog(this)
        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("profile.jpg")

        storageRef.putFile(imageUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                updateProfile(it.toString())
            }.addOnFailureListener {
                Config.hideDialog()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Config.hideDialog()
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(imageUrl: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val data = UserModel(
            name = binding.userName.text.toString(),
            email = binding.userEmail.text.toString(),
            city = binding.userCity.text.toString(),
            number = binding.userNumber.text.toString(),
            age = binding.userAge.text.toString(),
            image = imageUrl ?: ""
        )

        FirebaseDatabase.getInstance().getReference("users").child(currentUser!!.phoneNumber!!)
            .setValue(data).addOnCompleteListener {
                Config.hideDialog()
                if (it.isSuccessful) {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, it.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
