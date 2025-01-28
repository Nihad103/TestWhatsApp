package com.example.testwhatsapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.testwhatsapp.databinding.ItemChatBinding
import com.example.testwhatsapp.model.ChatList
import com.example.testwhatsapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat

class ChatListAdapter(private var chatList: List<ChatList>) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    private val differCallBack = object : DiffUtil.ItemCallback<ChatList>() {
        override fun areItemsTheSame(oldItem: ChatList, newItem: ChatList): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItem: ChatList, newItem: ChatList): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallBack)

    private var onItemClickListener: ((User) -> Unit)? = null

    class ChatListViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val chat = differ.currentList[position]

        holder.binding.textViewLastMessage.text = chat.lastMessage
        holder.binding.receiverNameTextView.text = chat.receiverName
        holder.binding.timeTextView.text = SimpleDateFormat("HH:mm").format(chat.lastMessageTimestamp)

        if (chat.receiverName == "Unknown User") {
            Toast.makeText(
                holder.itemView.context,
                "This user no longer exists.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            holder.itemView.setOnClickListener {
                findUserByName(chat.receiverName) { user ->
                    onItemClickListener?.let { it(user) }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener: (User) -> Unit) {
        onItemClickListener = listener
    }

    fun submitList(newList: List<ChatList>) {
        differ.submitList(newList)
    }

    private fun findUserByName(receiverName: String, callback: (User) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users")
        userRef.orderByChild("name").equalTo(receiverName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.children.firstOrNull()?.getValue(User::class.java)
                    user?.let { callback(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatListAdapter", "Error fetching user by name: ${error.message}")
                }
            })
    }
}
