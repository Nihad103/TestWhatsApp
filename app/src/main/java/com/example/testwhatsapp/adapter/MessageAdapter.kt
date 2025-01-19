package com.example.testwhatsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.testwhatsapp.R
import com.example.testwhatsapp.databinding.ItemMessageBinding
import com.example.testwhatsapp.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private val messageList: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.binding.messageTextView.text = message.content

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.format(Date(message.timestamp))
        holder.binding.timeTextView.text = time

        val params = holder.binding.messageTextView.layoutParams as ConstraintLayout.LayoutParams
        if (message.sender == "Me") {
            holder.binding.messageTextView.setBackgroundResource(R.drawable.message_background)
            params.startToStart = ConstraintLayout.LayoutParams.UNSET
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            params.horizontalBias = 0.1f
        } else {
            holder.binding.messageTextView.setBackgroundResource(R.drawable.message_background)
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET
            params.horizontalBias = 0.9f
        }
        holder.binding.messageTextView.layoutParams = params
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}


