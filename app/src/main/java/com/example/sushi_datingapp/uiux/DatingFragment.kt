package com.example.sushi_datingapp.uiux

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sushi_datingapp.adapter.DatingAdapter
import com.example.sushi_datingapp.databinding.FragmentDatingBinding
import com.example.sushi_datingapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DatingFragment : Fragment() {

    private lateinit var binding: FragmentDatingBinding
    private lateinit var datingRecyclerView: RecyclerView
    private lateinit var datingAdapter: DatingAdapter

    private lateinit var itemTouchHelper: ItemTouchHelper
    companion object {
        val userList = ArrayList<UserModel>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDatingBinding.inflate(inflater, container, false)
        datingRecyclerView = binding.datingRecyclerView
        setupRecyclerView()
        getData()
        return binding.root
    }

    private fun setupRecyclerView() {
        datingRecyclerView.layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean {
                return false // Disable vertical scrolling
            }
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, // Disable drag and drop
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val model = userList[position]

                    if (direction == ItemTouchHelper.LEFT) {
                        Toast.makeText(
                            requireContext(),
                            "Disliked ${model.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // TODO: Handle dislike action (e.g., send to Firebase)
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        Toast.makeText(requireContext(), "Liked ${model.name}", Toast.LENGTH_SHORT)
                            .show()
                        // TODO: Handle like action (e.g., send to Firebase)
                    }

                    // Animate swiped card off the screen
                    animateSwipe(viewHolder.itemView, direction)

                    // Remove swiped item from the list
                    userList.removeAt(position)
                    datingAdapter.notifyItemRemoved(position)

                    if (position == userList.size) {
                        Toast.makeText(
                            requireContext(),
                            "No more cards to show",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                // Override to control the card-like behavior
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val ratio = dX / recyclerView.width
                    val rotationAngle = 15f * ratio

                    // Rotate and translate the card
                    itemView.rotation = rotationAngle
                    itemView.translationX = dX

                    // Scale the card slightly
                    val scaleFactor = 1 - Math.abs(ratio) * 0.1f
                    itemView.scaleX = scaleFactor
                    itemView.scaleY = scaleFactor

                    // Fade out the card as it is swiped
                    itemView.alpha = 1 - Math.abs(ratio)
                } else {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                // Reset card rotation, translation, and scale after swipe operation ends
                viewHolder.itemView.rotation = 0f
                viewHolder.itemView.translationX = 0f
                viewHolder.itemView.scaleX = 1f
                viewHolder.itemView.scaleY = 1f
            }
        }

        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(datingRecyclerView)

        datingAdapter = DatingAdapter(requireContext(), userList, itemTouchHelper,)
        datingRecyclerView.adapter = datingAdapter
        datingRecyclerView.itemAnimator = null // Disable default item animator for custom animations
    }

    private fun animateSwipe(view: View, direction: Int) {
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat("alpha", 1f, 0f), // Fade out
            PropertyValuesHolder.ofFloat(
                "translationX",
                0f,
                if (direction == ItemTouchHelper.RIGHT) 1000f else -1000f
            ),
            PropertyValuesHolder.ofFloat(
                "rotation",
                if (direction == ItemTouchHelper.RIGHT) 15f else -15f
            ) // Rotate slightly
        )
        animator.duration = 300
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // Remove item view from RecyclerView
                view.alpha = 1f
                view.translationX = 0f
                view.rotation = 0f
            }
        })
        animator.start()
    }

    private fun getData() {
        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    for (postSnapshot in snapshot.children) {
                        val user = postSnapshot.getValue(UserModel::class.java)
                        user?.let { userList.add(it) }
                    }
                    datingAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }
}
