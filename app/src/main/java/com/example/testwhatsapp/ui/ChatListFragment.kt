package com.example.testwhatsapp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testwhatsapp.adapter.ChatListAdapter
import com.example.testwhatsapp.adapter.UserAdapter
import com.example.testwhatsapp.databinding.FragmentChatListBinding
import com.example.testwhatsapp.model.User
import com.example.testwhatsapp.repository.UserRepository
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
    private val auth: FirebaseAuth by inject()
    private val chatListViewModel: ChatListViewModel by inject()
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var userAdapter: UserAdapter
    private val allUsers = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBarTitle()
        setupAdapters()
        setupObservers()
        setupSearchView()

        loadChatList()
        loadAllUsers()
    }

    private fun setupAdapters() {
        chatListAdapter = ChatListAdapter(emptyList())
        binding.chatRecyclerView.apply {
            adapter = chatListAdapter
            layoutManager = LinearLayoutManager(context)
        }
        userAdapter = UserAdapter { user ->
            val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(user.id)
            findNavController().navigate(action)
        }
        binding.usersRecyclerView.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(context)
        }
        chatListAdapter.setOnItemClickListener { user ->
            val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(user.id)
            findNavController().navigate(action)
        }
    }

    private fun setupObservers() {
        chatListViewModel.chatList.observe(viewLifecycleOwner) { chatList ->
            if (chatList.isEmpty()) {
                Log.d("ChatListFragment", "Chat list is empty")
                binding.noResultsTextView.visibility = View.VISIBLE
            } else {
                Log.d("ChatListFragment", "Chat list size: ${chatList.size}")
                binding.noResultsTextView.visibility = View.GONE
            }
            chatListAdapter.submitList(chatList)
        }

    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterUsers(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    resetUserSearch()
                } else {
                    filterUsers(newText)
                }
                return true
            }
        })
    }

    private fun filterUsers(query: String) {
        val filteredList = allUsers.filter { it.name.contains(query, ignoreCase = true) }
        if (filteredList.isEmpty()) {
            binding.usersRecyclerView.visibility = View.GONE
            binding.noResultsTextView.visibility = View.VISIBLE
            binding.chatRecyclerView.visibility = View.GONE
            binding.noResultsTextView.text = "No Result"
        } else {
            userAdapter.updateList(filteredList)
            binding.usersRecyclerView.visibility = View.VISIBLE
            binding.noResultsTextView.visibility = View.GONE
            binding.chatRecyclerView.visibility = View.GONE
        }
    }

    private fun resetUserSearch() {
        userAdapter.updateList(allUsers)
        binding.chatRecyclerView.visibility = View.VISIBLE
        binding.usersRecyclerView.visibility = View.GONE
        binding.noResultsTextView.visibility = View.GONE
    }

    private fun loadChatList() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("ChatListFragment", "Current userId: $userId")

        chatListViewModel.loadChatList(userId)
    }

    private fun loadAllUsers() {
        val userRepository = UserRepository()
        userRepository.loadAllUsers({ users ->
            val currentUserId = auth.currentUser?.uid
            allUsers.clear()
            allUsers.addAll(users.filter { it.id != currentUserId })
            userAdapter.updateList(allUsers)
        }, { error ->
            Log.e("ChatListFragment", "Error loading users: ${error.message}")
        })
    }

    private fun setupActionBarTitle() {
        val currentUser = auth.currentUser ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").value as? String
                (activity as? AppCompatActivity)?.supportActionBar?.title = userName ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatListFragment", "Failed to fetch user name: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
