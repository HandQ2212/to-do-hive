package com.proptit.todohive.ui.home.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.proptit.todohive.R
import com.proptit.todohive.databinding.FragmentProfileBinding
import com.proptit.todohive.ui.MainActivity
import com.proptit.todohive.ui.home.profile.changeimage.BottomsheetChangeImage
import com.proptit.todohive.ui.home.profile.changename.BottomsheetChangeName
import com.proptit.todohive.ui.home.profile.changepassword.BottomsheetChangePassword

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentProfileBinding.bind(view)

        viewModel.uiState.observe(viewLifecycleOwner) { s ->
            binding.tvName.text = s.displayName
            binding.tvTaskLeft.text = "${s.tasksLeft} Task left"
            binding.tvTaskDone.text = "${s.tasksDone} Task done"

            if (s.avatarUrl.isNullOrBlank()) {
                binding.imgAvatar.setImageResource(R.drawable.ic_person)
            } else {
                Glide.with(binding.imgAvatar)
                    .load(s.avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(binding.imgAvatar)
            }
        }

        binding.rowLogout.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("app", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("remember_me", false)
                .remove("current_user_id")
                .apply()

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("nav_auth", "register")
            startActivity(intent)
            requireActivity().finish()
        }

        binding.rowChangeName.setOnClickListener {
            val currentName = viewModel.uiState.value?.displayName ?: ""
            val tag = "change_name_sheet"

            if (childFragmentManager.findFragmentByTag(tag) == null) {
                BottomsheetChangeName(currentName) { newName ->
                    viewModel.updateDisplayName(newName)
                }.show(childFragmentManager, tag)
            }
        }

        binding.rowChangePassword.setOnClickListener {
            val tag = "change_password_sheet"
            if (childFragmentManager.findFragmentByTag(tag) == null) {
                BottomsheetChangePassword { oldPw, newPw ->
                    viewModel.updatePasswordHash(oldPw, newPw) { ok, msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }.show(childFragmentManager, tag)
            }
        }

        binding.rowChangeImage.setOnClickListener {
            val tag = "change_image_sheet"
            if (childFragmentManager.findFragmentByTag(tag) == null) {
                BottomsheetChangeImage { action ->
                    when (action) {
                        BottomsheetChangeImage.Action.CAMERA -> {
                            // TODO: mở camera
                        }

                        BottomsheetChangeImage.Action.GALLERY -> {
                            // TODO: mở picker lấy ảnh
                        }

                        BottomsheetChangeImage.Action.DRIVE -> {
                            // TODO: SAF/Drive picker
                        }
                    }
                }.show(childFragmentManager, tag)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}