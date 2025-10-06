package com.proptit.todohive.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.proptit.todohive.R
import com.proptit.todohive.databinding.FragmentProfileBinding
import com.proptit.todohive.ui.MainActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentProfileBinding.bind(view)

        binding.btnCreateAccount.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("nav_auth", "register")
            val prefs = requireContext().getSharedPreferences("app", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("remember_me", false)
                .remove("current_user_id")
                .apply()
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}