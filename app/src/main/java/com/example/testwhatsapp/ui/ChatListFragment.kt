package com.example.testwhatsapp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testwhatsapp.R
import com.example.testwhatsapp.adapter.ChatListAdapter
import com.example.testwhatsapp.adapter.UserAdapter
import com.example.testwhatsapp.databinding.FragmentChatListBinding
import com.example.testwhatsapp.model.User
import com.example.testwhatsapp.viewmodel.ChatListViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.koin.android.ext.android.inject

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var userAdapter: UserAdapter
    private val users = mutableListOf<User>()
    private lateinit var auth: FirebaseAuth
    private val allUsers = mutableListOf<User>()

    private val chatListViewModel: ChatListViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }
        setupSearchView()
        setupRecyclerView()
        observeUsers()
        setupActionBarTitle()
    }

    private fun observeUsers() {
        chatListViewModel.fetchUsers().observe(viewLifecycleOwner, Observer { userList: List<User> ->
            allUsers.clear() // Əvvəlki siyahını təmizlə
            users.clear()

            if (userList != null && userList.isNotEmpty()) {
                allUsers.addAll(userList) // Bütün istifadəçiləri saxla

                // Aktiv çatları olan istifadəçiləri filtr edin
                val filteredList = userList.filter { user ->
                    user.chats?.any { chat -> chat.value.lastMessage?.isNotEmpty() == true } == true
                }

                // Son mesajın zamanına görə sıralayın
                val sortedList = filteredList.sortedByDescending { user ->
                    user.chats?.values?.maxByOrNull { it.lastMessageTimestamp }?.lastMessageTimestamp ?: 0L
                }

                users.addAll(sortedList)
                userAdapter.updateList(users)
                chatListAdapter.updateList(users)
                userAdapter.notifyDataSetChanged()
                chatListAdapter.notifyDataSetChanged()
            } else {
                Log.d("ChatListFragment", "No users found")
            }
        })
    }


    private fun setupRecyclerView() {
        // UserAdapter
        userAdapter = UserAdapter { user ->
            Log.d("ChatListFragment", "chatList to chat with userId: ${user.name}")
            val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(user.id)
            findNavController().navigate(action)
        }
        binding.usersRecyclerView.adapter = userAdapter
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(context)

        // ChatListAdapter
        chatListAdapter = ChatListAdapter(users)
        binding.chatRecyclerView.adapter = chatListAdapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.chatRecyclerView.visibility = View.VISIBLE
        chatListAdapter.setOnItemClickListener { user ->
            val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(user.id)
            findNavController().navigate(action)
        }
    }


    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    filterUsers(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    userAdapter.updateList(users)
                    binding.chatRecyclerView.visibility = View.VISIBLE
                    binding.usersRecyclerView.visibility = View.GONE
                    binding.noResultsTextView.visibility = View.GONE
                } else {
                    filterUsers(newText)
                }
                return true
            }
        })
    }

    private fun filterUsers(query: String) {
        val filteredList = allUsers.filter {
            it.name.contains(query, ignoreCase = true)
        }
        userAdapter.updateList(filteredList)
        if (filteredList.isEmpty()) {
            binding.usersRecyclerView.visibility = View.GONE
            binding.noResultsTextView.visibility = View.VISIBLE
            binding.noResultsTextView.text = "No user found"
        } else {
            binding.usersRecyclerView.visibility = View.VISIBLE
            binding.noResultsTextView.visibility = View.GONE
            binding.chatRecyclerView.visibility = View.GONE
        }
        Log.d("FilterUsers", "Filtered list size: ${filteredList.size}")
    }

    private fun setupActionBarTitle() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").value as? String
                    if (userName != null) {
                        (activity as AppCompatActivity).supportActionBar?.title = userName
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatListFragment", "Failed to fetch user name: ${error.message}")
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
