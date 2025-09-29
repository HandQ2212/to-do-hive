package com.proptit.todohive.ui.home.task

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.proptit.todohive.R
import com.proptit.todohive.common.SwipeToDeleteCallback
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.databinding.FragmentIndexBinding

class IndexFragment : Fragment() {

    private var _binding: FragmentIndexBinding? = null
    private val binding get() = _binding!!

    private var undoAnimator: ValueAnimator? = null
    private val UNDO_DURATION = 4000L

    private val taskViewModel: TasksViewModel by viewModels {
        TasksViewModelFactory(requireContext().applicationContext)
    }

    private val adapter by lazy { TaskAdapter { task -> taskViewModel.onToggleDone(task) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIndexBinding.inflate(inflater, container, false)
        binding.viewModel = taskViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        observeTasks()
        attachSwipeToDelete()
        initUndoTimerBar()
        setupFilterMenu()
    }

    private fun setupRecyclerView() {
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
    }

    private fun observeTasks() {
        taskViewModel.filteredTasks.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    private fun attachSwipeToDelete() {
        val swipe = SwipeToDeleteCallback(requireContext()) { vh ->
            val pos = vh.bindingAdapterPosition
            val item = adapter.currentList.getOrNull(pos) ?: return@SwipeToDeleteCallback
            taskViewModel.onSwipeDelete(item.task)
            showDeletionSnackbar(item.task)
        }
        ItemTouchHelper(swipe).attachToRecyclerView(binding.rvTasks)
    }

    private fun showDeletionSnackbar(task: TaskEntity) {
        val snack = Snackbar
            .make(binding.root, getString(R.string.deleted_fmt, task.title), Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                taskViewModel.restore(task)
                stopUndoTimer()
            }
            .setAnchorView(R.id.fabAdd)
            .setDuration(UNDO_DURATION.toInt())

        customizeSnackbar(snack)

        snack.addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar) { startUndoTimer() }
            override fun onDismissed(tb: Snackbar, event: Int) { stopUndoTimer() }
        })

        snack.show()
    }

    private fun customizeSnackbar(snack: Snackbar) {
        val ctx = requireContext()
        snack.view.backgroundTintList =
            android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.gray))

        val tv = snack.view.findViewById<android.widget.TextView>(
            com.google.android.material.R.id.snackbar_text
        )

        ContextCompat.getDrawable(ctx, R.drawable.ic_warning)?.mutate()?.let { d ->
            DrawableCompat.setTint(d, ContextCompat.getColor(ctx, R.color.warning))
            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null)
            tv.compoundDrawablePadding = (8 * resources.displayMetrics.density).toInt()
            tv.maxLines = 3
        }
    }

    private fun initUndoTimerBar() {
        binding.undoTimer.isIndeterminate = false
        binding.undoTimer.max = 1000
        binding.undoTimer.progress = 1000
    }

    private fun startUndoTimer() {
        binding.undoTimer.apply {
            isVisible = true
            max = 1000
            progress = 1000
        }
        undoAnimator?.cancel()
        undoAnimator = ValueAnimator.ofInt(1000, 0).apply {
            duration = UNDO_DURATION
            addUpdateListener { anim -> binding.undoTimer.progress = anim.animatedValue as Int }
            start()
        }
    }

    private fun stopUndoTimer() {
        undoAnimator?.cancel()
        undoAnimator = null
        binding.undoTimer.isVisible = false
    }

    private fun setupFilterMenu() {
        binding.tvFilter.setOnClickListener { anchor ->
            PopupMenu(requireContext(), anchor).apply {
                menuInflater.inflate(R.menu.menu_filter, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_today     -> taskViewModel.setFilter(Filter.TODAY)
                        R.id.action_tomorrow  -> taskViewModel.setFilter(Filter.TOMORROW)
                        R.id.action_yesterday -> taskViewModel.setFilter(Filter.YESTERDAY)
                        R.id.action_completed -> taskViewModel.setFilter(Filter.COMPLETED)
                        else -> return@setOnMenuItemClickListener false
                    }
                    true
                }
            }.show()
        }
    }

    override fun onDestroyView() {
        stopUndoTimer()
        _binding = null
        super.onDestroyView()
    }
}