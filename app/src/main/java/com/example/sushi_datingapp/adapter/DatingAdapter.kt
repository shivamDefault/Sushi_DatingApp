package com.example.sushi_datingapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sushi_datingapp.activity.ChatActivity
import com.example.sushi_datingapp.databinding.ItemUserLayoutBinding
import com.example.sushi_datingapp.model.UserModel
import com.google.firebase.database.FirebaseDatabase

class DatingAdapter(
    private val context: Context,
    private var userList: ArrayList<UserModel>, // Mutable list to hold all users
    private val itemTouchHelper: ItemTouchHelper
) : RecyclerView.Adapter<DatingAdapter.DatingViewHolder>() {

    inner class DatingViewHolder(val binding: ItemUserLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatingViewHolder {
        val binding = ItemUserLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DatingViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: DatingViewHolder, position: Int) {
        val user = userList[position]

        // Bind user data to views
        holder.binding.textView6.text = user.name
        holder.binding.textView5.text = user.email
        Glide.with(context).load(user.image).into(holder.binding.userImage)

        // Handle chat button click
        holder.binding.chat.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("userId", user.number)
                putExtra("receiverImage", user.image)
            }
            context.startActivity(intent)
        }

        // Handle heart button click
        holder.binding.heart.setOnClickListener {
            toggleLike(user, holder)
            animateHeart(holder.binding.heart)
        }
    }

    private fun toggleLike(user: UserModel, holder: DatingViewHolder) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(user.number!!)
        userRef.child("liked").setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Liked ${user.name}", Toast.LENGTH_SHORT).show()
                moveCard(holder)
            } else {
                Toast.makeText(context, "Failed to like ${user.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveCard(holder: DatingViewHolder) {
        // Implement swipe action programmatically (if needed)
        itemTouchHelper.startSwipe(holder)
    }

    private fun animateHeart(heartView: ImageView) {
        val scaleAnimation = ScaleAnimation(
            1f, 1.5f, 1f, 1.5f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300
            interpolator = BounceInterpolator()
            repeatCount = 1
            repeatMode = Animation.REVERSE
        }

        heartView.startAnimation(scaleAnimation)
    }

    // Method to filter the user list based on gender
    fun filterByGender(gender: String) {
        userList = ArrayList(userList.filter { it.gender.equals(gender, ignoreCase = true) })
        notifyDataSetChanged()
    }
}
