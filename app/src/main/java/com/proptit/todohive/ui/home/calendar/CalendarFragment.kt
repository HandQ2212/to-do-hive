package com.proptit.todohive.ui.home.calendar

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar
import com.proptit.todohive.R
import com.proptit.todohive.common.SwipeToDeleteCallback
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.databinding.FragmentCalendarBinding
import com.proptit.todohive.repository.TaskRepository
import com.proptit.todohive.ui.home.task.TaskAdapter

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels {
        val ctx = requireContext().applicationContext
        val db = AppDatabase.get(ctx)
        val userId = ctx.getSharedPreferences("app", Context.MODE_PRIVATE)
            .getLong("current_user_id", 0L)
        require(userId > 0L) { "No logged-in user." }
        CalendarViewModel.Factory(TaskRepository(ctx, db, userId))
    }

    private val taskAdapter by lazy {
        TaskAdapter(
            onToggleDone = { task -> viewModel.onToggleDone(task) },
            onClick = { task -> openTaskDetail(task.task_id) },
        )
    }

    private lateinit var weekAdapter: WeekDayAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentCalendarBinding.bind(view)
        setupSegmented()
        setupTasksRecycler()
        setupWeekStrip()
        observeHeader()
        observeTasks()
        attachSwipeToDelete()
    }

    private fun setupSegmented() {
        binding.segFilter.check(R.id.btnSegToday)
        binding.segFilter.addOnButtonCheckedListener { _: MaterialButtonToggleGroup, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnSegToday     -> viewModel.setCompletedFilter(false)
                R.id.btnSegCompleted -> viewModel.setCompletedFilter(true)
            }
        }
    }

    private fun setupTasksRecycler() {
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupWeekStrip() {
        weekAdapter = WeekDayAdapter(onClick = { day -> viewModel.selectDate(day) })
        binding.rvWeekDays.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = weekAdapter
            setHasFixedSize(true)
        }
        viewModel.weekDays.observe(viewLifecycleOwner) { days ->
            weekAdapter.submitList(days)
            weekAdapter.setSelected(viewModel.selectedDate.value)
        }
        viewModel.selectedDate.observe(viewLifecycleOwner) { sel ->
            weekAdapter.setSelected(sel)
        }
        viewModel.daysWithTasksInWeek.observe(viewLifecycleOwner) { dates ->
            weekAdapter.setTaskDates(dates)
        }
        binding.btnPrevWeek.setOnClickListener { viewModel.previousWeek() }
        binding.btnNextWeek.setOnClickListener { viewModel.nextWeek() }
    }

    private fun observeHeader() {
        viewModel.monthTitle.observe(viewLifecycleOwner) { title ->
            binding.tvMonth.text = title
        }
        viewModel.selectedDateLabel.observe(viewLifecycleOwner) { label ->
            binding.tvSelectedDate.text = label
        }
    }

    private fun observeTasks() {
        viewModel.tasksForSelectedDate.observe(viewLifecycleOwner) { list ->
            taskAdapter.submitList(list)
            binding.tvEmpty.isVisible = list.isEmpty()
        }
    }

    private fun attachSwipeToDelete() {
        val swipe = SwipeToDeleteCallback(requireContext()) { viewHolder ->
            val pos = viewHolder.bindingAdapterPosition
            val item = taskAdapter.currentList.getOrNull(pos) ?: return@SwipeToDeleteCallback
            val task = item.task
            viewModel.onSwipeDelete(task)
            showDeletionSnackbar(task)
        }
        ItemTouchHelper(swipe).attachToRecyclerView(binding.rvTasks)
    }

    private fun showDeletionSnackbar(task: TaskEntity) {
        Snackbar.make(binding.root, getString(R.string.deleted_fmt, task.title), Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.fabAdd)
            .setAction(R.string.undo) { viewModel.restore(task) }
            .show()
    }

    private fun openTaskDetail(taskId: Long) {
        val action = CalendarFragmentDirections.actionCalendarFragmentToTaskFragment(taskId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}