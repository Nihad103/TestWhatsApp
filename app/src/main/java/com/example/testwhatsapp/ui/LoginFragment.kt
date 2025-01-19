package com.example.testwhatsapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.testwhatsapp.R
import com.example.testwhatsapp.databinding.FragmentLoginBinding
import com.example.testwhatsapp.viewmodel.LoginViewModel
import org.koin.android.ext.android.inject

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var sharedPref: SharedPreferences
    private val loginViewModel: LoginViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        sharedPref = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("rememberMe", false)) {
            navController.navigate(R.id.action_loginFragment_to_chatListFragment)
        }
        setupObservers()
        loginBtnClick()
        registerBtnClick()
    }

    private fun setupObservers() {
        loginViewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                navController.navigate(R.id.action_loginFragment_to_chatListFragment)
            }
        }

        loginViewModel.loginErrorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginBtnClick() {
        binding.loginButton.setOnClickListener {
            val userName = binding.username.text.toString().trim()
            if (userName.isNotEmpty()) {
                val email = "$userName@test.com"
                val password = "defaultPassword123"
                val rememberMe = binding.checkBoxRememberMe.isChecked
                loginViewModel.loginUser(email, password, rememberMe)
            } else {
                Toast.makeText(context, "Please enter your username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerBtnClick() {
        binding.registerButton.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
