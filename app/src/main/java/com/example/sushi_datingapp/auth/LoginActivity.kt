package com.example.sushi_datingapp.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sushi_datingapp.MainActivity
import com.example.sushi_datingapp.R
import com.example.sushi_datingapp.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: AlertDialog
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dialog = AlertDialog.Builder(this).setView(R.layout.loading_layout).create()

        binding.sendOtp.setOnClickListener {
            val phoneNumber = binding.userNumber.text.toString().trim()
            if (phoneNumber.isEmpty()) {
                binding.userNumber.error = "Please enter your number"
            } else {
                sendOtp(phoneNumber)
            }
        }

        binding.verifyOtp.setOnClickListener {
            val otp = binding.userOtp.text.toString().trim()
            if (otp.isEmpty()) {
                binding.userOtp.error = "Please enter your OTP"
            } else {
                verifyOtp(otp)
            }
        }
    }

    private fun sendOtp(number: String) {
        dialog.show()
        val phoneNumber = "+91$number" // Ensure the phone number includes the country code
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                dialog.dismiss()
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                dialog.dismiss()
                Toast.makeText(this@LoginActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "Verification failed", e)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                dialog.dismiss()
                this@LoginActivity.verificationId = verificationId
                binding.numberLayout.visibility = GONE
                binding.otpLayout.visibility = VISIBLE
                Toast.makeText(this@LoginActivity, "Verification code sent", Toast.LENGTH_SHORT).show()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtp(otp: String) {
        if (verificationId == null) {
            Toast.makeText(this, "Verification ID is null", Toast.LENGTH_SHORT).show()
            return
        }
        dialog.show()
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                dialog.dismiss()
                if (task.isSuccessful) {
                    checkUserExist(binding.userNumber.text.toString())
                } else {
                    Toast.makeText(this, "Sign in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserExist(number: String) {
        dialog.show()
        val phoneNumber = "+91$number"
        FirebaseDatabase.getInstance().getReference("users").child(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dialog.dismiss()
                    if (snapshot.exists()) {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
                        intent.putExtra("phoneNumber", phoneNumber)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dialog.dismiss()
                    Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
