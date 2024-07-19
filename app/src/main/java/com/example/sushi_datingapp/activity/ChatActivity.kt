package com.example.sushi_datingapp.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sushi_datingapp.adapter.ChatAdapter
import com.example.sushi_datingapp.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRef: DatabaseReference
    private lateinit var messageList: ArrayList<HashMap<String, Any>>
    private lateinit var chatId: String
    private val PICK_IMAGE_REQUEST = 1 // Request code for image selection

    // Result launcher for image selection
    private val getActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                uploadImageToStorage(imageUri) // Send image to Firebase Storage
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val receiverId = intent.getStringExtra("userId")
        val receiverImage = intent.getStringExtra("userImage")
        val senderId = FirebaseAuth.getInstance().currentUser?.phoneNumber

        if (receiverId == null || senderId == null) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        chatId = getChatId(senderId, receiverId)
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        messageList = ArrayList()

        // Initialize RecyclerView
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(this, messageList)
        binding.chatRecyclerView.adapter = chatAdapter

        // Load messages
        loadMessages()

        // Load receiver image
        if (!receiverImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(receiverImage)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(100, 100)) // Specify the dimensions

                .into(binding.userImage)
        }

        // Handle back button click
        binding.backButton.setOnClickListener {
            finish()
        }

        // Send message functionality
        binding.imageView4.setOnClickListener {
            val message = binding.yourMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendChat(message)
            }
        }

        // Image selection
        binding.imageView2.setOnClickListener {
            selectImage()
        }

        // Emoji functionality (to be implemented)
        binding.imageView3.setOnClickListener {
            // Implement emoji picker or selection logic here
            // Example: Use a library like EmojiPickerView
        }
    }

    // Function to generate chat ID based on sender and receiver IDs
    private fun getChatId(senderId: String, receiverId: String): String {
        return if (senderId < receiverId) {
            "$senderId-$receiverId"
        } else {
            "$receiverId-$senderId"
        }
    }

    // Function to handle image selection
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        getActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    // Function to upload image to Firebase Storage
    private fun uploadImageToStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    sendMessage("", uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@ChatActivity,
                    "Image upload failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Function to send a text message
    private fun sendChat(message: String) {
        sendMessage(message, "")
    }

    // Function to send message to Firebase Database
    private fun sendMessage(message: String, imageUrl: String) {
        val senderId = FirebaseAuth.getInstance().currentUser?.phoneNumber
        if (senderId == null) {
            Toast.makeText(this, "Sender ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val currentTime = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date())
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        val map = hashMapOf<String, Any>(
            "message" to message,
            "senderId" to senderId,
            "imageUrl" to imageUrl,
            "currentTime" to currentTime,
            "currentDate" to currentDate
        )

        chatRef.push().setValue(map)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.yourMessage.text!!.clear()
                    binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
                } else {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to load messages from Firebase Database
    private fun loadMessages() {
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.value as HashMap<String, Any>
                    messageList.add(message)
                }
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ChatActivity,
                    "Failed to load messages: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
