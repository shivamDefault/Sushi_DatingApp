package com.example.sushi_datingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sushi_datingapp.databinding.ChatItemBinding
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(
    private val context: Context, private val list: ArrayList<HashMap<String, Any>>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentMessage = list[position]
        val senderId = currentMessage["senderId"] as String
        val message = currentMessage["message"] as String
        val imageUrl = currentMessage["imageUrl"] as? String
        val currentTime = currentMessage["currentTime"] as String
        val currentDate = currentMessage["currentDate"] as String

        if (senderId == FirebaseAuth.getInstance().currentUser?.phoneNumber) {
            // User's message
            holder.binding.chatItemRight.visibility = View.VISIBLE
            holder.binding.chatItemLeft.visibility = View.GONE
            holder.binding.chatTextRight.text = message
            holder.binding.timeRight.text = "$currentDate $currentTime"

            if (imageUrl != null) {
                holder.binding.chatImageRight.visibility = View.VISIBLE
                Glide.with(context).load(imageUrl).into(holder.binding.chatImageRight)
            } else {
                holder.binding.chatImageRight.visibility = View.GONE
            }
        } else {
            // Other user's message
            holder.binding.chatItemRight.visibility = View.GONE
            holder.binding.chatItemLeft.visibility = View.VISIBLE
            holder.binding.chatTextLeft.text = message
            holder.binding.timeLeft.text = "$currentDate $currentTime"

            if (imageUrl != null) {
                holder.binding.chatImageLeft.visibility = View.VISIBLE
                Glide.with(context).load(imageUrl).into(holder.binding.chatImageLeft)
            } else {
                holder.binding.chatImageLeft.visibility = View.GONE
            }
        }
    }
}
