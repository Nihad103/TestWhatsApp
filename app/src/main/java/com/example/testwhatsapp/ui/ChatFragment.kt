package com.example.testwhatsapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testwhatsapp.R
import com.example.testwhatsapp.adapter.MessageAdapter
import com.example.testwhatsapp.databinding.FragmentChatBinding
import com.example.testwhatsapp.model.Call
import com.example.testwhatsapp.model.Message
import com.example.testwhatsapp.viewmodel.ChatViewModel
import com.example.testwhatsapp.webrtc.VoiceCallActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var userId: String? = null
    private val PICK_MEDIA_REQUEST = 101
    private val REQUEST_CODE = 100
    private val auth: FirebaseAuth by inject()
    private val chatViewModel: ChatViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
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
        setupAttachMediaButton()

        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
            )
        }
        binding.voiceCallButton.setOnClickListener {
            startVoiceCall()
        }
    }

    private fun startVoiceCall() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE
            )
        } else {

            val receiverId = userId ?: return
            val callerId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            val callId = "$callerId-$receiverId"
            val channelName = "channel3"
            val token =
                "007eJxTYPDLbPp6XX5zSIHcBVn3Srfjwoq/j5S1HT7Q579v5v2a+Y8VGMxNLVLNUgwNzQwszUwMTc0TjZMszE1TkpITzc0sk0wsF89bkt4QyMhwf1kPCyMDBIL4HAzJGYl5eak5xgwMAC0uIpg="

            val callRef = FirebaseDatabase.getInstance().getReference("calls").child(callId)

            val call = Call(
                callId = callId,
                callerId = callerId,
                receiverId = receiverId,
                status = "ringing",
                timestamp = System.currentTimeMillis(),
                channelName = channelName,
                token = token
            )

            callRef.setValue(call).addOnSuccessListener {
                Toast.makeText(context, "Calling...", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, VoiceCallActivity::class.java).apply {
                    putExtra("callId", callId)
                    putExtra("callerId", callerId)
                    putExtra("receiverId", receiverId)
                    putExtra("channelName", channelName)
                    putExtra("isCaller", true)
                    putExtra("token", token)
                }
                startActivity(intent)
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Call failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceCall()
            } else {
                Toast.makeText(context, "Mikrofon icazəsi lazımdır", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAttachMediaButton() {
        binding.attachMediaButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_MEDIA_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { mediaUri ->
                val shortenedFileName = getShortenedFileName(mediaUri)
                Log.d("ChatFragment", "Shortened file name: $shortenedFileName")
                uploadMediaToDatabase(mediaUri)
            }
        }
    }

    private fun getShortenedFileName(uri: Uri): String {
        val fileName = uri.lastPathSegment
        return fileName?.take(50) ?: "unknown.jpg"
    }

    private fun uploadMediaToDatabase(mediaUri: Uri) {
        val base64Image = encodeImageToBase64(mediaUri)

        if (base64Image != null) {
            sendMessageWithMedia(base64Image)
        } else {
            Toast.makeText(context, "Media encoding failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encodeImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun sendMessageWithMedia(mediaBase64: String) {
        val chatId = createChatId(auth.currentUser?.uid ?: "", userId ?: "")
        val newMessage = Message(
            messageId = "",
            sender = auth.currentUser?.uid ?: "Unknown",
            content = "",
            mediaContent = mediaBase64,
            timestamp = System.currentTimeMillis(),
            messageType = "media"
        )

        userId?.let { receiverId ->
            chatViewModel.sendMessage(chatId, newMessage, receiverId)
        }
    }

    private fun setupRecyclerView(currentUserId: String) {
        messageAdapter = MessageAdapter(messages, currentUserId)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.messageRecyclerView.adapter = messageAdapter
        messageAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeMessages(chatId: String) {
        chatViewModel.fetchMessages(chatId)
            .observe(viewLifecycleOwner, Observer { messageList ->
                messages.clear()
                messages.addAll(messageList)
                messageAdapter.notifyDataSetChanged()
                binding.messageRecyclerView.scrollToPosition(messages.size - 1)
            })
    }

    private fun getUserName(userId: String?) {
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().getReference("users").child(userId)
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").value as? String
                    binding.userNameTextView.text = userName ?: "Unknown User"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        "Username could not be retrieved: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
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