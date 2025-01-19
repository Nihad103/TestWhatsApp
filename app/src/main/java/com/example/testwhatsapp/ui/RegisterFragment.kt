package com.example.testwhatsapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.testwhatsapp.R
import com.example.testwhatsapp.databinding.FragmentRegisterBinding
import com.example.testwhatsapp.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private val registerViewModel: RegisterViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        setupObservers()
        registerBtnClick()
    }

    private fun setupObservers() {
        registerViewModel.registerResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                navController.navigate(R.id.action_registerFragment_to_chatListFragment)
            }
        }

        registerViewModel.registerErrorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerBtnClick() {
        binding.registerButton.setOnClickListener {
            val userName = binding.userName.text.toString().trim()
            if (userName.isEmpty()) {
                Toast.makeText(context, "Lütfen bir kullanıcı adı girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerViewModel.registerUser(
                email = "$userName@test.com",
                password = "defaultPassword123",
                userName = userName,
                rememberMe = binding.checkBoxRememberMe.isChecked
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
