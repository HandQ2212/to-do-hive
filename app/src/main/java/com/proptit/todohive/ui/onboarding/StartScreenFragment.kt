package com.proptit.todohive.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.proptit.todohive.R
import com.proptit.todohive.databinding.FragmentStartScreenBinding

class StartScreenFragment : Fragment() {

    private var _binding: FragmentStartScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        applyInsets()
        setupToolbar()
        setupLogin()
        setupCreateAccount()
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

    private fun setupLogin() {
        binding.btnLogin.setOnClickListener {
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.startScreenFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_to_login,
                null,
                navOptions
            )
        }
    }

    private fun setupCreateAccount() {
        binding.btnCreateAccount.setOnClickListener {
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.startScreenFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_startScreenFragment_to_registerFragment,
                null,
                navOptions
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}