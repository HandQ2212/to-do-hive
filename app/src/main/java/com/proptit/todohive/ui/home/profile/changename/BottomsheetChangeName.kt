package com.proptit.todohive.ui.home.profile.changename

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.databinding.BottomsheetChangeNameBinding

class BottomsheetChangeName(
    private val currentName: String,
    private val onEdit: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetChangeNameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChangeNameSheetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetChangeNameBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setInitialName(currentName)

        binding.edtName.doAfterTextChanged { viewModel.onNameChanged(it) }

        viewModel.isSaveEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.btnEdit.isEnabled = enabled
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnEdit.setOnClickListener {
            val newName = viewModel.getFinalName()
            if (!newName.isNullOrEmpty()) {
                onEdit(newName)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
