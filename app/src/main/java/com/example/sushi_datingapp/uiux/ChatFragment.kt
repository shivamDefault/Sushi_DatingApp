package com.example.sushi_datingapp.uiux

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sushi_datingapp.activity.ChatActivity
import com.example.sushi_datingapp.adapter.MessageUserAdapter
import com.example.sushi_datingapp.databinding.FragmentChatBinding
import com.example.sushi_datingapp.model.UserModel
import com.example.sushi_datingapp.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment(), MessageUserAdapter.OnItemClickListener {
    private lateinit var binding: FragmentChatBinding
    private val userList = mutableListOf<UserModel>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.phoneNumber
    private lateinit var adapter: MessageUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ChatFragment", "View Created")

        // Initialize the adapter and set it to RecyclerView
        adapter = MessageUserAdapter(requireContext(), userList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        getData()
    }

    private fun getData() {
        Config.showDialog(requireContext())
        val chatsRef = FirebaseDatabase.getInstance().getReference("chats")
        chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                Log.d("ChatFragment", "Chats DataSnapshot: ${snapshot.childrenCount}")
                for (chatSnapshot in snapshot.children) {
                    val chatKey = chatSnapshot.key
                    if (chatKey != null && chatKey.contains(currentUserId!!)) {
                        val parts = chatKey.split("-")
                        val otherUserId = parts.find { it != currentUserId }
                        Log.d("ChatFragment", "Chat key: $chatKey, Other user ID: $otherUserId")
                        if (otherUserId != null) {
                            val userRef = FirebaseDatabase.getInstance().getReference("users")
                                .child(otherUserId)
                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    if (userSnapshot.exists()) {
                                        val user = userSnapshot.getValue(UserModel::class.java)
                                        if (user != null) {
                                            userList.add(user)
                                            Log.d("ChatFragment", "User added: ${user.name}")
                                            adapter.notifyDataSetChanged() // Notify the adapter
                                        }
                                    } else {
                                        Log.d("ChatFragment", "User does not exist: $otherUserId")
                                    }

                                    // Update RecyclerView after adding user
                                    if (isAdded) {
                                        updateRecyclerViewVisibility()
                                        Config.hideDialog()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("ChatFragment", "Error fetching user: ${error.message}")
                                    Toast.makeText(
                                        requireContext(),
                                        "Error fetching user: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Config.hideDialog()
                                }
                            })
                        }
                    }
                }
                // Show/hide no chats message based on userList size
                updateRecyclerViewVisibility()
                Config.hideDialog()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragment", "Failed to fetch chats: ${error.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch chats: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Config.hideDialog()
            }
        })
    }

    private fun updateRecyclerViewVisibility() {
        if (userList.isEmpty()) {
            binding.noChatsMessage.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            Log.d("ChatFragment", "No users found")
        } else {
            binding.noChatsMessage.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            Log.d("ChatFragment", "Users found: ${userList.size}")
        }
    }

    override fun onItemClick(user: UserModel) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("userId", user.number)
        intent.putExtra("userImage", user.image)
        startActivity(intent)
    }
}
