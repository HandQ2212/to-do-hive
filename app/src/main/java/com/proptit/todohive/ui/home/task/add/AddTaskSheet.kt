package com.proptit.todohive.ui.home.task.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.R
import com.proptit.todohive.databinding.BottomsheetAddTaskBinding
import com.proptit.todohive.ui.home.task.PickCategorySheet
import com.proptit.todohive.ui.home.task.PickPrioritySheet
import com.proptit.todohive.ui.home.task.PickTimeSheet

class AddTaskSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetAddTaskBinding? = null
    private val binding get() = _binding!!
    private val addTaskSheetViewModel: AddTaskSheetViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetAddTaskBinding.inflate(inflater, container, false)
        binding.viewModel = addTaskSheetViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun navController() =
        requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.findNavController() ?: findNavController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        ivTime.setOnClickListener {
            PickTimeSheet().show(parentFragmentManager, "pick_time")
        }
        ivTag.setOnClickListener {
            PickCategorySheet().show(parentFragmentManager, "pick_category")
        }
        ivFlag.setOnClickListener {
            PickPrioritySheet().show(parentFragmentManager, "pick_priority")
        }
        ivSend.setOnClickListener {
            addTaskSheetViewModel.save(
                onSuccess = { dismiss() },
                onError = {
                    // TODO: show error/snackbar if needed
                }
            )
        }
    }

    override fun onDestroyView() {
        _binding = null; super.onDestroyView()
    }
}