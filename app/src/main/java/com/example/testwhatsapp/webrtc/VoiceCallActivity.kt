package com.example.testwhatsapp.webrtc

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testwhatsapp.R
import com.example.testwhatsapp.model.User
import com.google.firebase.database.*
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine

class VoiceCallActivity : AppCompatActivity() {

    private lateinit var tvCallerName: TextView
    private lateinit var btnAcceptCall: Button
    private lateinit var btnDeclineCall: Button
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
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@VoiceCallActivity, "User offline", Toast.LENGTH_SHORT).show()
                endCall()
            }
        }

        override fun onLeaveChannel(stats: RtcStats) {
            runOnUiThread {
                tvCallerName.text = "Call ended"
                endCall()
            }
        }

        override fun onError(err: Int) {
            runOnUiThread {
                Toast.makeText(this@VoiceCallActivity, "Error: $err", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)

        tvCallerName = findViewById(R.id.tvCallerName)
        btnAcceptCall = findViewById(R.id.btnAcceptCall)
        btnDeclineCall = findViewById(R.id.btnDeclineCall)

        // Intent-dən gələn məlumatları alırıq
        callId = intent.getStringExtra("callId") ?: ""
        callerId = intent.getStringExtra("callerId") ?: ""
        receiverId = intent.getStringExtra("receiverId") ?: ""
        channelName = intent.getStringExtra("channelName") ?: callId
        token = intent.getStringExtra("token") ?: ""

        // Zəng edənin adını göstər
        getCallerName(callerId)

        // Agora Engine-i inicializasiya edirik
        initializeAgoraEngine()

        // Zəng qəbul düyməsi
        btnAcceptCall.setOnClickListener {
            acceptCall()
        }

        // Zəng rədd düyməsi
        btnDeclineCall.setOnClickListener {
            declineCall()
        }
    }

    private fun getCallerName(callerId: String) {
        FirebaseDatabase.getInstance().getReference("users").child(callerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    tvCallerName.text = "${user?.name} is calling..."
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun initializeAgoraEngine() {
        try {
            agoraEngine = RtcEngine.create(baseContext, AGORA_APP_ID, rtcEventHandler)
            agoraEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            agoraEngine?.enableAudio()
        } catch (e: Exception) {
            Toast.makeText(this, "Agora SDK initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun acceptCall() {
        // Zəngin statusunu "answered" edirik
        onCallAnswered(callId)
        database.child(callId).child("status").setValue("accepted").addOnSuccessListener {
            // Agora zənginə qoşuluruq
            agoraEngine?.joinChannel(token, channelName, "", 0)
            Toast.makeText(this, "Call accepted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun declineCall() {
        // Zəngin statusunu "declined" edirik
        onCallRejected(callId)
        database.child(callId).child("status").setValue("declined").addOnSuccessListener {
            Toast.makeText(this, "Call declined!", Toast.LENGTH_SHORT).show()
            endCall()
        }
    }

    private fun endCall() {
        // Zəngin bitdiyini bildirmək
        onCallEnded(callId)
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

    // Zəngin cavablandığını bildirmək
    private fun onCallAnswered(callId: String) {
        database.child(callId).child("status").setValue("answered")
    }

    // Zəngin bitdiyini bildirmək
    private fun onCallEnded(callId: String) {
        database.child(callId).child("status").setValue("ended")
    }

    // Zəng rədd edildikdə
    private fun onCallRejected(callId: String) {
        database.child(callId).child("status").setValue("rejected")
    }
}
