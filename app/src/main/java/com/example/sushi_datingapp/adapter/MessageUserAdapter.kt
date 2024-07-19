package com.example.sushi_datingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sushi_datingapp.databinding.UserItemLayoutBinding
import com.example.sushi_datingapp.model.UserModel

class MessageUserAdapter(
    private val context: Context,
    private val userList: List<UserModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MessageUserAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(user: UserModel)
    }

    inner class ViewHolder(private val binding: UserItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(userList[position])
                }
            }
        }

        fun bind(user: UserModel) {
            binding.userName.text = user.name
            binding.userEmail.text = user.email
            Glide.with(context).load(user.image).into(binding.userImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
