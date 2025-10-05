package com.proptit.todohive.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.proptit.todohive.R
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.UserEntity
import com.proptit.todohive.databinding.FragmentRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyInsets()
        setupToolbar()
        setupButtons()
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupButtons() {
        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvLogin.setOnClickListener { findNavController().navigateUp() }
    }

    private fun attemptRegister() {
        val username = binding.edtUsername.text?.toString()?.trim().orEmpty()
        val password = binding.edtPassword.text?.toString().orEmpty()
        val confirm = binding.edtConfirm.text?.toString().orEmpty()

        clearErrors()

        if (!validateInputs(username, password, confirm)) return

        lifecycleScope.launch { registerUser(username, password) }
    }

    private fun clearErrors() {
        binding.tilUsername.error = null
        binding.tilPassword.error = null
        binding.tilConfirm.error = null
    }

    private fun validateInputs(username: String, password: String, confirm: String): Boolean {
        if (username.isEmpty()) {
            binding.tilUsername.error = "Please enter username"
            return false
        }
        if (!isStrongPassword(password)) {
            binding.tilPassword.error = "Min 8 chars, include lowercase, uppercase and special character"
            return false
        }
        if (password != confirm) {
            binding.tilConfirm.error = "Passwords do not match"
            return false
        }
        return true
    }

    private suspend fun registerUser(username: String, password: String) {
        val dao = AppDatabase.get(requireContext()).userDao()
        val exists = withContext(Dispatchers.IO) { dao.existsByUsername(username) }

        if (exists) {
            binding.tilUsername.error = "Username is already taken"
            return
        }

        val hashed = hashPassword(password)
        val user = UserEntity(
            username = username,
            password_hash = hashed,
            email = "$username@local"
        )

        val insertedOk = withContext(Dispatchers.IO) {
            try {
                dao.insert(user)
                true
            } catch (t: Throwable) {
                false
            }
        }

        if (insertedOk) {
            onRegisterSuccess(username, password)
        } else {
            Toast.makeText(requireContext(), "Failed to register", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRegisterSuccess(username: String, password: String) {
        Toast.makeText(requireContext(), "Registered successfully", Toast.LENGTH_SHORT).show()
        val options = androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.registerFragment, true)
            .build()
        val args = bundleOf(
            "prefill_username" to username,
            "prefill_password" to password
        )
        findNavController().navigate(R.id.loginFragment, args, options)
    }

    private fun isStrongPassword(pw: String): Boolean {
        val regex = Regex(
            """^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).{8,}$"""
        )
        return regex.containsMatchIn(pw)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}