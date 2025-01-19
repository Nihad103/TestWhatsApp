package com.example.testwhatsapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testwhatsapp.R
import com.example.testwhatsapp.adapter.MessageAdapter
import com.example.testwhatsapp.databinding.FragmentChatBinding
import com.example.testwhatsapp.model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var database: DatabaseReference
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        setHasOptionsMenu(true)
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = arguments?.getString("userId")
        setupRecyclerView()
        setupSendButton()
        fetchMessages(userId)
    }

    private fun fetchMessages(userId: String?) {
        if (userId != null) {
            database = FirebaseDatabase.getInstance().getReference("users").child(userId)
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").value as? String
                    if (userName != null) {
                        binding.userNameTextView.text = userName
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "userName null", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.messageRecyclerView.adapter = messageAdapter
        messages.addAll(getDummyMessages())
        messageAdapter.notifyDataSetChanged()
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
//            sendMessage(userId)
            val messageContent = binding.messageEditText.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                val newMessage = Message(
                    id = messages.size.toString(),
                    sender = "Me",
                    content = messageContent,
                    timestamp = System.currentTimeMillis()
                )
                messages.add(newMessage)
                messageAdapter.notifyItemInserted(messages.size - 1)
                binding.messageRecyclerView.scrollToPosition(messages.size - 1)
                binding.messageEditText.text.clear()
            }
        }
    }

    private fun getDummyMessages(): List<Message> {
        return listOf(
            Message(id = "1", sender = "User A", content = "Hello!", timestamp = 14012025),
            Message(id = "2", sender = "User B", content = "Hi there!", timestamp = 14012025)
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_delete_account)?.isVisible = false
        menu.findItem(R.id.action_logout)?.isVisible = false
    }

//    private fun setupInsetsListener() {
//        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
//            val rect = android.graphics.Rect()
//            binding.root.getWindowVisibleDisplayFrame(rect)
//            val screenHeight = binding.root.rootView.height
//            val keypadHeight = screenHeight - rect.bottom
//            if (keypadHeight > screenHeight * 0.15) {
//                binding.bottomContainer.translationY = -keypadHeight.toFloat()
//            } else {
//                binding.bottomContainer.translationY = 0f
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

