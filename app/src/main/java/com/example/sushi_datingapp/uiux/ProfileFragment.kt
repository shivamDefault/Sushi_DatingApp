package com.example.sushi_datingapp.uiux


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.sushi_datingapp.R
import com.example.sushi_datingapp.activity.EditProfileActivity
import com.example.sushi_datingapp.auth.LoginActivity

import com.example.sushi_datingapp.databinding.FragmentProfileBinding
import com.example.sushi_datingapp.model.UserModel
import com.example.sushi_datingapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Config.showDialog(requireContext())
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.phoneNumber!!).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val data = it.getValue(UserModel::class.java)
                        binding.nameTextView.text ="Name:  "+ data?.name
                        binding.emailTextView.text = "Email:  "+data?.email
                        binding.numberTextView.text = "Number:  "+data?.number
                        binding.cityTextView.text = "City:  "+data?.city
                        binding.ageTextView.text ="Age:  "+ data?.age
                        binding.genderTextView.text = "Gender:  "+data?.gender
                        binding.statusTextView.text ="Relationship:  "+ data?.relationship
                        binding.zodiacTextView.text ="Zodiac:  "+ data?.zodiac
                        Glide.with(requireContext()).load(data?.image).placeholder(R.drawable.man)
                            .into(binding.userImage)
                    } else {
                        Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                    Config.hideDialog()
                }.addOnFailureListener {
                    Config.hideDialog()
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            Config.hideDialog()
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        binding.editProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        return binding.root
    }
}