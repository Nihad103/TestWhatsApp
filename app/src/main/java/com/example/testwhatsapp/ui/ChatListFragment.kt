package com.example.testwhatsapp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testwhatsapp.adapter.ChatListAdapter
import com.example.testwhatsapp.adapter.UserAdapter
import com.example.testwhatsapp.databinding.FragmentChatListBinding
import com.example.testwhatsapp.model.Message
import com.example.testwhatsapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var userAdapter: UserAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val users = mutableListOf<User>()

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
        setupSearchView()
        setupRecyclerView()
        fetchUsers()
        setupActionBarTitle()
    }

    private fun fetchUsers() {
        database = FirebaseDatabase.getInstance().getReference("users")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                userAdapter.updateList(users)
                Log.d("FetchUsers", "Total users: ${users.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                binding.noResultsTextView.text = "Error: ${error.message}"
                binding.noResultsTextView.visibility = View.VISIBLE
                binding.usersRecyclerView.visibility = View.GONE
                binding.chatRecyclerView.visibility = View.GONE
                Log.e("FetchUsers", "Error: ${error.message}")
            }
        })
    }

    private fun setupRecyclerView() {
        val messages = getDummyMessages()
        userAdapter = UserAdapter { user ->
            Log.d("ChatListFragment", "chatList to chat with userId: ${user.name}")
            val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(user.id)
            findNavController().navigate(action)
        }
        binding.usersRecyclerView.adapter = userAdapter
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(context)

        chatListAdapter = ChatListAdapter(messages)
        binding.chatRecyclerView.adapter = chatListAdapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)

        chatListAdapter.setOnItemClickListener { message ->
            val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(message.id)
            findNavController().navigate(action)
        }
        chatListAdapter.updateList(messages)
    }

    private fun getDummyMessages(): List<Message> {
        return listOf(
            Message(id = "1", sender = "User A", content = "Hello!", timestamp = 14012025),
            Message(id = "2", sender = "User B", content = "Hi there!", timestamp = 14012025),
            Message(id = "3", sender = "User C", content = "Hi there!", timestamp = 14012025),
            Message(id = "4", sender = "User D", content = "Hi there!", timestamp = 14012025),
            Message(id = "5", sender = "User E", content = "Hi there!", timestamp = 14012025),
            Message(id = "6", sender = "User F", content = "Hi there!", timestamp = 14012025),
            Message(id = "7", sender = "User G", content = "Hi there!", timestamp = 14012025),
            Message(id = "8", sender = "User H", content = "Hi there!", timestamp = 14012025),
            Message(id = "9", sender = "User I", content = "Hi there!", timestamp = 14012025)
        )
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
        val filteredList = users.filter {
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
