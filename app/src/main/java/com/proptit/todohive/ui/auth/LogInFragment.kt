package com.proptit.todohive.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.proptit.todohive.R
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.databinding.FragmentLogInBinding
import com.proptit.todohive.ui.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class LogInFragment : Fragment() {

    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefillData()
        applyInsets()
        setupToolbar()
        setupLoginButton()
        setupRegisterLink()
    }

    private fun prefillData() {
        val u = arguments?.getString("prefill_username").orEmpty()
        val p = arguments?.getString("prefill_password").orEmpty()
        if (u.isNotEmpty()) binding.edtUsername.setText(u)
        if (p.isNotEmpty()) binding.edtPassword.setText(p)

        val prefs = requireContext().getSharedPreferences("app", android.content.Context.MODE_PRIVATE)
        val remembered = prefs.getBoolean("remember_me", false)
        val rememberedUsername = prefs.getString("remember_username", "") ?: ""

        if (u.isEmpty() && rememberedUsername.isNotEmpty()) {
            binding.edtUsername.setText(rememberedUsername)
        }
        binding.cbRemember.isChecked = remembered
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewLifecycleOwner.lifecycleScope.launch { checkLogin(username, password) }
        }
    }

    private fun setupRegisterLink() {
        binding.tvRegister.setOnClickListener {
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment, null, navOptions)
        }
    }

    private suspend fun checkLogin(username: String, password: String) {
        val dao = AppDatabase.get(requireContext()).userDao()
        val inputHash = hashPassword(password.trim())

        val user = withContext(Dispatchers.IO) { dao.findByUsername(username.trim()) }
        if (user == null) {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (user.password_hash == inputHash) {
            val prefs = requireContext().getSharedPreferences("app", android.content.Context.MODE_PRIVATE)
            if (binding.cbRemember.isChecked) {
                prefs.edit()
                    .putBoolean("remember_me", true)
                    .putLong("remember_user_id", user.user_id)
                    .putString("remember_username", user.username)
                    .putString("remember_hash", user.password_hash)
                    .apply()
            } else {
                prefs.edit()
                    .remove("remember_me")
                    .remove("remember_user_id")
                    .remove("remember_username")
                    .remove("remember_hash")
                    .apply()
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Welcome, ${user.username}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), HomeActivity::class.java))
                requireActivity().finish()
            }
        } else {
            Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}