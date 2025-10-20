package com.proptit.todohive.ui.home.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.proptit.todohive.App
import com.proptit.todohive.BuildConfig
import com.proptit.todohive.R
import com.proptit.todohive.databinding.FragmentProfileBinding
import com.proptit.todohive.ui.MainActivity
import com.proptit.todohive.ui.home.profile.changeimage.BottomsheetChangeImage
import com.proptit.todohive.ui.home.profile.changename.BottomsheetChangeName
import com.proptit.todohive.ui.home.profile.changepassword.BottomsheetChangePassword
import java.io.File

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }
    private var cameraUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            res.data?.data?.let { uri -> uploadWithCloudinary(uri) }
        }
    }
    private val driveLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            uploadWithCloudinary(uri)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) cameraUri?.let { uploadWithCloudinary(it) }
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
                        BottomsheetChangeImage.Action.CAMERA -> openCamera()
                        BottomsheetChangeImage.Action.GALLERY -> openGallery()
                        BottomsheetChangeImage.Action.DRIVE -> openDrive()
                    }
                }.show(childFragmentManager, tag)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        galleryLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"))
    }

    private fun openDrive() {
        driveLauncher.launch(arrayOf("image/*"))
    }

    private fun openCamera() {
        val dir = File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val file = File.createTempFile("avatar_", ".jpg", dir)
        cameraUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        cameraLauncher.launch(cameraUri)
    }

    private fun setUploading(ui: Boolean) {
        binding.rowChangeImage.isEnabled = !ui
    }

    private fun uploadWithCloudinary(uri: Uri) {
        setUploading(true)
        MediaManager.get()
            .upload(uri)
            .unsigned(BuildConfig.UPLOAD_PRESET)
            .option("upload_preset", BuildConfig.UPLOAD_PRESET)
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    setUploading(false)
                    val secureUrl = (resultData?.get("secure_url") as? String).orEmpty()
                    if (secureUrl.isBlank()) {
                        Toast.makeText(requireContext(), "Không nhận được secure_url", Toast.LENGTH_LONG).show()
                        return
                    }
                    viewModel.updateAvatarUrl(secureUrl)
                    Glide.with(binding.imgAvatar)
                        .load(secureUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.imgAvatar)
                    Toast.makeText(requireContext(), "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show()
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    setUploading(false)
                    Toast.makeText(requireContext(), "Upload lỗi: ${error?.description}", Toast.LENGTH_LONG).show()
                    Log.e("ProfileFragment", "Upload lỗi: ${error?.description}")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch(requireContext())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}