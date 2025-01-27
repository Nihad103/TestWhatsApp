package com.example.testwhatsapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testwhatsapp.R
import com.example.testwhatsapp.adapter.MessageAdapter
import com.example.testwhatsapp.databinding.FragmentChatBinding
import com.example.testwhatsapp.model.Message
import com.example.testwhatsapp.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.koin.android.ext.android.inject

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var database: DatabaseReference
    private var userId: String? = null
    private val auth: FirebaseAuth by inject()
    private val chatViewModel: ChatViewModel by inject()

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
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }
        userId = arguments?.getString("userId")
        val chatId = createChatId(currentUserId.toString(), userId ?: "")
        setupRecyclerView(currentUserId)
        setupSendButton(chatId)
        observeMessages(chatId)
        getUserName(userId)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeMessages(chatId: String) {
        chatViewModel.fetchMessages(chatId).observe(viewLifecycleOwner, Observer { messageList -> messages.clear()
        messages.addAll(messageList)
            messageAdapter.notifyDataSetChanged()
            binding.messageRecyclerView.scrollToPosition(messages.size - 1)
        })
    }

    private fun getUserName(userId: String?) {
        if (userId != null) {
            database = FirebaseDatabase.getInstance().getReference("users").child(userId)
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").value as? String
                    if (userName != null) {
                        binding.userNameTextView.text = userName
                    } else {
                        binding.userNameTextView.text = "Unknown User"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Username could not be retrieved: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView(currentUserId: String) {
        messageAdapter = MessageAdapter(messages, currentUserId)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.messageRecyclerView.adapter = messageAdapter
        messageAdapter.notifyDataSetChanged()
    }

    private fun setupSendButton(chatId: String) {
        binding.sendButton.setOnClickListener {
            val messageContent = binding.messageEditText.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                val newMessage = Message(
                    messageId = "",
                    sender = auth.currentUser?.uid ?: "Unknown",
                    content = messageContent,
                    timestamp = System.currentTimeMillis()
                )
                userId?.let { receiverId ->
                    chatViewModel.sendMessage(chatId, newMessage, receiverId)
                }
                binding.messageEditText.text.clear()
            }
        }
    }

    private fun createChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "$userId1-$userId2" else "$userId2-$userId1"
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_delete_account)?.isVisible = false
        menu.findItem(R.id.action_logout)?.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
