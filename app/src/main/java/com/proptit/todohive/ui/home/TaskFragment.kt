package com.proptit.todohive.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.proptit.todohive.databinding.FragmentTaskBinding
import com.proptit.todohive.ui.home.task.PickCategorySheet
import com.proptit.todohive.ui.home.task.PickPrioritySheet
import com.proptit.todohive.ui.home.task.PickTimeSheet
import com.proptit.todohive.ui.home.task.TaskDetailViewModel
import com.proptit.todohive.ui.home.task.edit.EditTaskSheet
import java.time.Instant

class TaskFragment : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskDetailViewModel by viewModels {
        TaskDetailViewModel.Factory(requireContext().applicationContext)
    }

    companion object {
        const val REQ_TIME = "req_time"
        const val RES_TIME_MS = "res_time_ms"
        const val REQ_CATEGORY = "req_category"
        const val RES_CATEGORY_ID = "res_category_id"
        const val RES_CATEGORY_NAME = "res_category_name"
        const val REQ_PRIORITY = "req_priority"
        const val RES_PRIORITY = "res_priority"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindActions()
        observeViewModel()
        setupResultListeners()
        loadTaskFromArgs()
    }

    private fun bindActions() {
        binding.onBack = { findNavController().navigateUp() }
        binding.onShare = { shareCurrentTask() }
        binding.onToggleDone = { viewModel.toggleDone() }
        binding.onEditTitle = {
            openEditTaskSheet(
                currentTitle = binding.taskTitle ?: viewModel.taskTitle.value ?: "",
                currentDesc = binding.taskDesc ?: viewModel.taskDesc.value ?: ""
            )
        }
        binding.onPickTime = { PickTimeSheet().show(parentFragmentManager, "pick_time") }
        binding.onPickCategory = { PickCategorySheet().show(parentFragmentManager, "pick_category") }
        binding.onPickPriority = { PickPrioritySheet().show(parentFragmentManager, "pick_priority") }
        binding.onDelete = {
            viewModel.deleteTask()
            Snackbar.make(binding.root, "Đã xoá task", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
        binding.onEditTask = {
            viewModel.editTask()
            Snackbar.make(binding.root, "Đã lưu thay đổi", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.taskTitle.observe(viewLifecycleOwner) { binding.taskTitle = it }
        viewModel.taskDesc.observe(viewLifecycleOwner) { binding.taskDesc = it }
        viewModel.taskTimeText.observe(viewLifecycleOwner) { binding.taskTimeText = it }
        viewModel.categoryText.observe(viewLifecycleOwner) { binding.categoryText = it }
        viewModel.priorityText.observe(viewLifecycleOwner) { binding.priorityText = it }
        viewModel.isDone.observe(viewLifecycleOwner) { binding.isDone = it }
        viewModel.category.observe(viewLifecycleOwner) { binding.category = it }
    }

    private fun setupResultListeners() {
        parentFragmentManager.setFragmentResultListener(EditTaskSheet.REQ_EDIT_TASK, viewLifecycleOwner) { _, bundle ->
            val newTitle = bundle.getString(EditTaskSheet.RES_EDIT_TITLE, "")
            val newDesc = bundle.getString(EditTaskSheet.RES_EDIT_DESC, "")
            viewModel.editTask(newTitle, newDesc)
        }
        parentFragmentManager.setFragmentResultListener(REQ_TIME, viewLifecycleOwner) { _, bundle ->
            val epochMs = bundle.getLong(RES_TIME_MS, -1L)
            if (epochMs > 0) viewModel.pickTime(Instant.ofEpochMilli(epochMs))
        }
        parentFragmentManager.setFragmentResultListener(REQ_CATEGORY, viewLifecycleOwner) { _, bundle ->
            val catId = bundle.getLong(RES_CATEGORY_ID, -1L).let { if (it < 0) null else it }
            val catName = bundle.getString(RES_CATEGORY_NAME)
            viewModel.pickCategory(catId, catName)
        }
        parentFragmentManager.setFragmentResultListener(REQ_PRIORITY, viewLifecycleOwner) { _, bundle ->
            val p = bundle.getInt(RES_PRIORITY, -1)
            if (p > 0) viewModel.pickPriority(p)
        }
    }

    private fun loadTaskFromArgs() {
        val taskId = requireArguments().getLong("task_id", -1L)
        if (taskId != -1L) viewModel.load(taskId)
    }

    private fun openEditTaskSheet(currentTitle: String, currentDesc: String) {
        EditTaskSheet.newInstance(currentTitle = currentTitle, currentDesc = currentDesc)
            .show(parentFragmentManager, "edit_task")
    }

    private fun shareCurrentTask() {
        val title = binding.taskTitle ?: ""
        val desc = binding.taskDesc ?: ""
        val shareText = buildString {
            appendLine(title)
            if (desc.isNotBlank()) appendLine(desc)
            binding.taskTimeText?.takeIf { it.isNotBlank() }?.let { appendLine("⏰ $it") }
            binding.categoryText?.takeIf { it.isNotBlank() }?.let { appendLine("🏷 $it") }
            binding.priorityText?.takeIf { it.isNotBlank() }?.let { appendLine(it) }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText.trim())
        }
        startActivity(Intent.createChooser(intent, "Share Task"))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}