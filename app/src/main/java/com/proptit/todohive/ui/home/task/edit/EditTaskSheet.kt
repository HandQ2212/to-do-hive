package com.proptit.todohive.ui.home.task.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.R
import com.proptit.todohive.databinding.BottomsheetEditTaskBinding

class EditTaskSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetEditTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditTaskSheetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NO_TITLE,
            com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog
        )

        if (savedInstanceState == null) {
            val initTitle = arguments?.getString(ARG_TITLE).orEmpty()
            val initDesc  = arguments?.getString(ARG_DESC).orEmpty()
            viewModel.title.value = initTitle
            viewModel.description.value = initDesc
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEditTaskBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUi()
        setupActions()
    }

    private fun setupUi() {
        binding.etTitle.requestFocus()
    }

    private fun setupActions() {
        binding.ivSend.setOnClickListener {
            val title = viewModel.title.value?.trim().orEmpty()
            val desc  = viewModel.description.value?.trim().orEmpty()

            if (title.isBlank()) {
                binding.tilTitle.error = getString(R.string.error_title_required)
                return@setOnClickListener
            } else {
                binding.tilTitle.error = null
            }

            parentFragmentManager.setFragmentResult(
                REQ_EDIT_TASK,
                bundleOf(
                    RES_EDIT_TITLE to title,
                    RES_EDIT_DESC to desc
                )
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQ_EDIT_TASK   = "req_edit_task"
        const val RES_EDIT_TITLE  = "res_edit_title"
        const val RES_EDIT_DESC   = "res_edit_desc"

        const val ARG_TITLE = "arg_title"
        const val ARG_DESC  = "arg_desc"

        fun newInstance(
            currentTitle: String,
            currentDesc: String
        ): EditTaskSheet = EditTaskSheet().apply {
            arguments = bundleOf(
                ARG_TITLE to currentTitle,
                ARG_DESC to currentDesc
            )
        }
    }
}
