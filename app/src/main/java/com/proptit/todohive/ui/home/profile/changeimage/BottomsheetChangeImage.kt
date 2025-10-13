package com.proptit.todohive.ui.home.profile.changeimage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.databinding.BottomsheetChangeImageBinding

class BottomsheetChangeImage(
    private val onPick: (Action) -> Unit
) : BottomSheetDialogFragment() {

    enum class Action { CAMERA, GALLERY, DRIVE }

    private var _binding: BottomsheetChangeImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = BottomsheetChangeImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.optionCamera.setOnClickListener { onPick(Action.CAMERA); dismiss() }
        binding.optionGallery.setOnClickListener { onPick(Action.GALLERY); dismiss() }
        binding.optionDrive.setOnClickListener { onPick(Action.DRIVE); dismiss() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
