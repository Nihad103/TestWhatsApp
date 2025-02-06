package com.example.testwhatsapp.webrtc

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testwhatsapp.R
import com.example.testwhatsapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

class VoiceCallActivity : AppCompatActivity() {

    private lateinit var tvCallerName: TextView
    private lateinit var btnEndCall: Button
    private lateinit var tvIncomingCallInfo: TextView
    private lateinit var callId: String
    private lateinit var callerId: String
    private lateinit var receiverId: String
    private lateinit var channelName: String
    private lateinit var token: String

    private val database = FirebaseDatabase.getInstance().getReference("calls")
    private var agoraEngine: RtcEngine? = null

    companion object {
        const val AGORA_APP_ID = "758e6d1160964157a3b875dbca769b49"
    }

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                tvCallerName.text = "Call connected"
                Toast.makeText(this@VoiceCallActivity, "Call connected", Toast.LENGTH_SHORT).show()
                Log.d("callconnection", "Call connected")
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@VoiceCallActivity, "User offline", Toast.LENGTH_SHORT).show()
                Log.d("callconnection", "User offline")

                endCall()
            }
        }

        override fun onLeaveChannel(stats: RtcStats) {
            runOnUiThread {
                tvCallerName.text = "Call ended"
                endCall()
            }
        }

        override fun onError(error: Int) {
            runOnUiThread {
                Toast.makeText(this@VoiceCallActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                Log.d("callconnection", "Error: $error\"")

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)
        tvCallerName = findViewById(R.id.tvCallerName)
        btnEndCall = findViewById(R.id.btnEndCall)
        tvIncomingCallInfo = findViewById(R.id.tvIncomingCallInfo)

        callId = intent.getStringExtra("callId") ?: ""
        callerId = intent.getStringExtra("callerId") ?: ""
        receiverId = intent.getStringExtra("receiverId") ?: ""
        channelName = intent.getStringExtra("channelName") ?: callId
        token = intent.getStringExtra("token") ?: ""

        getUserNameFromFirebase(callerId)
        updateIncomingCallInfo()
        btnEndCall.setOnClickListener {
            endCall()
        }
        initializeAgoraEngine()
    }

    private fun updateIncomingCallInfo() {
        if (callerId == FirebaseAuth.getInstance().currentUser?.uid) {
            getReceiverName()
        } else {
            getUserNameFromFirebase(callerId)
        }
    }

    private fun getUserNameFromFirebase(userId: String) {
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.child(userId).get().addOnSuccessListener { snapshot ->
            val userName = snapshot.child("name").value as? String
            userName?.let {
                tvIncomingCallInfo.text = "You are talking to $it"
            }
        }.addOnFailureListener {
            tvIncomingCallInfo.text = "Call info is null"
        }
    }

    private fun getReceiverName() {
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.child(receiverId).get().addOnSuccessListener { snapshot ->
            val userName = snapshot.child("name").value as? String
            userName?.let {
                tvIncomingCallInfo.text = "You are talking to $it"
            }
        }.addOnFailureListener {
            tvIncomingCallInfo.text = "Incoming call"
        }
    }

    private fun initializeAgoraEngine() {
        try {
            Log.d("Agora", "Agora Engine initializing...")
            agoraEngine = RtcEngine.create(baseContext, AGORA_APP_ID, rtcEventHandler)
            agoraEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            agoraEngine?.joinChannel(token, channelName, "", 0)
            agoraEngine?.enableAudio()
            Log.d("Agora", "Agora Engine initialized successfully")
        } catch (e: Exception) {
            Log.e("Agora", "Agora SDK initialization failed: ${e.message}")
            Toast.makeText(this, "Agora SDK initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun endCall() {
        onCallEnded(callId)
        deleteCallFromFirebase()
        agoraEngine?.leaveChannel()
        RtcEngine.destroy()
        agoraEngine = null
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        RtcEngine.destroy()
        agoraEngine = null
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {}

    private fun onCallEnded(callId: String) {
        database.child(callId).child("status").setValue("ended")
    }

    private fun deleteCallFromFirebase() {
        database.child(callId).removeValue().addOnFailureListener {
            Toast.makeText(this@VoiceCallActivity, "Failed to delete call data", Toast.LENGTH_SHORT)
                .show()
        }
    }
}