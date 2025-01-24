package com.example.testwhatsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.testwhatsapp.databinding.ItemChatBinding
import com.example.testwhatsapp.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(private var userList: List<User>) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val differCallBack = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    private var onItemClickListener: ((User) -> Unit)? = null

    class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val user = differ.currentList[position]

        // User name
        holder.binding.senderNameTextView.text = user.name

        // Get the most recent chat's last message
        val lastMessage = user.chats?.values?.maxByOrNull { it.lastMessageTimestamp }?.lastMessage
        holder.binding.textViewLastMessage.text = lastMessage ?: "No message yet"

        // Format the timestamp of the last message
        val lastMessageTimestamp = user.chats?.values?.maxByOrNull { it.lastMessageTimestamp }?.lastMessageTimestamp
        holder.binding.timeTextView.text = if (lastMessageTimestamp != null) formatTimestamp(lastMessageTimestamp) else ""

        // Handle item click
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(user) }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return if (timestamp > 0) {
            sdf.format(Date(timestamp))
        } else {
            ""
        }
    }

    fun setOnItemClickListener(listener: (User) -> Unit) {
        onItemClickListener = listener
    }

    fun updateList(newList: List<User>) {
        differ.submitList(newList)
    }
}