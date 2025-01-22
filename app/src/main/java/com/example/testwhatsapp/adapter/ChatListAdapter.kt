package com.example.testwhatsapp.adapter

import android.util.Log
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
        holder.binding.senderNameTextView.text = user.name
        if (user.lastMessage != null) {
            holder.binding.textViewLastMessage.text = user.lastMessage
        } else {
            Log.d("ChatListAdapter", "last message error")
            holder.binding.textViewLastMessage.text = "No message yet"
        }
        holder.binding.timeTextView.text = formatTimestamp(user.lastMessageTimestamp)
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
        notifyDataSetChanged()
    }
}
