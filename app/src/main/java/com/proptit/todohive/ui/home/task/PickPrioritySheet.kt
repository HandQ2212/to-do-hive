package com.proptit.todohive.ui.home.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.databinding.BottomsheetPickPriorityBinding
import com.proptit.todohive.ui.home.task.add.AddTaskSheetViewModel

class PickPrioritySheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetPickPriorityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTaskSheetViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetPickPriorityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        tvTitle.text = "Choose priority"
        btnP1.setOnClickListener {
            viewModel.setPickedPriority(1)
            dismiss()
        }
        btnP2.setOnClickListener {
            viewModel.setPickedPriority(2)
            dismiss()
        }
        btnP3.setOnClickListener {
            viewModel.setPickedPriority(3)
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}