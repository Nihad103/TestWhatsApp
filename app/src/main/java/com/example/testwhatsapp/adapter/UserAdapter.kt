package com.example.testwhatsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.testwhatsapp.databinding.ItemUserBinding
import com.example.testwhatsapp.model.User

class UserAdapter(private val onItemClickListener: (User) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val differCallBack = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallBack)

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = differ.currentList[position]
        holder.binding.userNameTextView.text = user.name
        holder.itemView.setOnClickListener {
            onItemClickListener(user)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun updateList(newList: List<User>) {
        differ.submitList(newList)
        notifyDataSetChanged()
    }
}