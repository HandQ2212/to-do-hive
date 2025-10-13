package com.proptit.todohive.ui.home.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.R
import com.proptit.todohive.common.SpacesItemDecoration
import com.proptit.todohive.databinding.BottomsheetPickPriorityBinding
import com.proptit.todohive.ui.home.TaskFragment
import com.proptit.todohive.ui.home.task.add.AddTaskSheetViewModel

class PickPrioritySheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetPickPriorityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTaskSheetViewModel by activityViewModels()

    private val adapter by lazy {
        PriorityAdapter { value -> tempSelected = value }
    }

    private var tempSelected: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetPickPriorityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupGrid()
        setupButtons()
        seedData()
    }

    private fun setupGrid() = with(binding) {
        rvPriorities.layoutManager = GridLayoutManager(requireContext(), 4)
        rvPriorities.adapter = adapter
        val space = resources.getDimensionPixelSize(R.dimen.grid_space_8)
        if (rvPriorities.itemDecorationCount == 0) {
            rvPriorities.addItemDecoration(SpacesItemDecoration(space, space))
        }
    }

    private fun setupButtons() = with(binding) {
        btnCancel.setOnClickListener { dismiss() }
        btnSave.setOnClickListener {
            viewModel.setPickedPriority(tempSelected)
            parentFragmentManager.setFragmentResult(
                TaskFragment.REQ_PRIORITY,
                bundleOf(TaskFragment.RES_PRIORITY to tempSelected)
            )
            dismiss()
        }
    }

    private fun seedData() {
        tempSelected = viewModel.pickedPriority.value ?: 1
        adapter.submitList((1..10).toList())
        adapter.setSelected(tempSelected)
        binding.tvTitle.text = getString(R.string.task_priority)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}