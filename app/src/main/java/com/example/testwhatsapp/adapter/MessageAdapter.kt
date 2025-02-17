package com.example.testwhatsapp.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testwhatsapp.R
import com.example.testwhatsapp.databinding.ItemMessageBinding
import com.example.testwhatsapp.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val messageList: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]

        if (message.messageType == "media") {
            holder.binding.messageTextView.visibility = View.GONE
            holder.binding.mediaImageView.visibility = View.VISIBLE

            val mediaContent = message.mediaContent
            if (mediaContent != null) {
                val byteArray = Base64.decode(mediaContent, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                holder.binding.mediaImageView.setImageBitmap(bitmap)
            } else {
                holder.binding.mediaImageView.setImageResource(R.drawable.error_image)
            }
        } else {
            holder.binding.messageTextView.visibility = View.VISIBLE
            holder.binding.mediaImageView.visibility = View.GONE
            holder.binding.messageTextView.text = message.content
        }


        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.format(Date(message.timestamp))
        holder.binding.timeTextView.text = time

        val params = holder.binding.messageTextView.layoutParams as ConstraintLayout.LayoutParams
        if (message.sender == currentUserId) {
            holder.binding.messageTextView.setBackgroundResource(R.drawable.message_background_right)
            params.startToStart = ConstraintLayout.LayoutParams.UNSET
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            params.horizontalBias = 1.0f
        } else {
            holder.binding.messageTextView.setBackgroundResource(R.drawable.message_background_left)
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET
            params.horizontalBias = 0.0f
        }
        holder.binding.messageTextView.layoutParams = params

        val timeParams = holder.binding.timeTextView.layoutParams as ConstraintLayout.LayoutParams
        if (message.sender == currentUserId) {
            timeParams.startToStart = ConstraintLayout.LayoutParams.UNSET
            timeParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            timeParams.horizontalBias = 1.0f
        } else {
            timeParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            timeParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
            timeParams.horizontalBias = 0.0f
        }
        holder.binding.timeTextView.layoutParams = timeParams
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}