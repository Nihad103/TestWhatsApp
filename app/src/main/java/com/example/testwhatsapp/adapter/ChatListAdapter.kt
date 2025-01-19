package com.example.testwhatsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.testwhatsapp.databinding.ItemChatBinding
import com.example.testwhatsapp.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(private var chatList: List<Message>) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val differCallBack = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)

    private var onItemClickListener: ((Message) -> Unit)? = null

    class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = differ.currentList[position]
        holder.binding.senderNameTextView.text = message.sender
        holder.binding.textViewLastMessage.text = message.content
        holder.binding.timeTextView.text = formatTimestamp(message.timestamp)
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(message) }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun setOnItemClickListener(listener: (Message) -> Unit) {
        onItemClickListener = listener
    }

    fun updateList(newList: List<Message>) {
        differ.submitList(newList)
    }
}
