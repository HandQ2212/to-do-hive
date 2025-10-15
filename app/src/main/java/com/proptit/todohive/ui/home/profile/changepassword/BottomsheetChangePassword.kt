package com.proptit.todohive.ui.home.profile.changepassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.databinding.BottomsheetChangePasswordBinding

class BottomsheetChangePassword(
    private val onSubmit: (oldPass: String, newPass: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val passwordViewModel: ChangePasswordSheetViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = BottomsheetChangePasswordBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = passwordViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.edtOld.doAfterTextChanged { passwordViewModel.onFieldChanged() }
        binding.edtNew.doAfterTextChanged { passwordViewModel.onFieldChanged() }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnEdit.setOnClickListener {
            passwordViewModel.getPair()?.let { (o, n) ->
                onSubmit(o, n)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
