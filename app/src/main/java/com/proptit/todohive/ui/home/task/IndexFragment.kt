package com.proptit.todohive.ui.home.task

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import com.proptit.todohive.R
import com.proptit.todohive.common.SwipeToDeleteCallback
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.databinding.FragmentIndexBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IndexFragment : Fragment() {

    private var _binding: FragmentIndexBinding? = null
    private val binding get() = _binding!!

    private var undoAnimator: ValueAnimator? = null

    private val taskViewModel: TasksViewModel by viewModels {
        TasksViewModel.Factory(requireContext().applicationContext)
    }

    private val adapter by lazy {
        TaskAdapter(
            onToggleDone = { task -> taskViewModel.onToggleDone(task) },
            onClick = { task -> openTaskDetail(task.task_id) }
        )
    }

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
        loadUserAvatar()
    }

    private fun setupRecyclerView() {
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
    }

    private fun observeTasks() {
        taskViewModel.filteredTasks.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    private fun attachSwipeToDelete() {
        val swipe = SwipeToDeleteCallback(requireContext()) { viewHolder ->
            val pos = viewHolder.bindingAdapterPosition
            val item = adapter.currentList.getOrNull(pos) ?: return@SwipeToDeleteCallback
            taskViewModel.onSwipeDelete(item.task)
            showDeletionSnackbar(item.task)
        }
        ItemTouchHelper(swipe).attachToRecyclerView(binding.rvTasks)
    }

    private fun showDeletionSnackbar(task: TaskEntity) {
        val snack = createBaseSnackbar(getString(R.string.deleted_fmt, task.title))
            .setAction(R.string.undo) {
                taskViewModel.restore(task)
                stopUndoTimer()
            }
        customizeSnackbar(snack)
        snack.addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar) { startUndoTimer() }
            override fun onDismissed(tb: Snackbar, event: Int) { stopUndoTimer() }
        })
        snack.show()
    }

    private fun createBaseSnackbar(message: String): Snackbar {
        return Snackbar
            .make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.fabAdd)
            .setDuration(UNDO_DURATION.toInt())
    }

    private fun customizeSnackbar(snack: Snackbar) {
        val ctx = requireContext()
        val view = snack.view
        view.background = MaterialShapeDrawable(
            ShapeAppearanceModel().toBuilder().setAllCornerSizes(dp(12f)).build()
        ).apply {
            fillColor = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.gray))
            elevation = dp(6f)
        }
        (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
            val m = dpInt(12f)
            lp.setMargins(m, m, m, m)
            view.layoutParams = lp
        }
        val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        val actionBtn = view.findViewById<Button>(com.google.android.material.R.id.snackbar_action)
        textView.setTextColor(ContextCompat.getColor(ctx, R.color.white))
        actionBtn.setTextColor(ContextCompat.getColor(ctx, R.color.warning))
        ContextCompat.getDrawable(ctx, R.drawable.ic_warning)?.mutate()?.let { d ->
            DrawableCompat.setTint(d, ContextCompat.getColor(ctx, R.color.warning))
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null)
            textView.compoundDrawablePadding = dpInt(8f)
            textView.maxLines = 3
        }
    }

    private fun initUndoTimerBar() {
        binding.undoTimer.isIndeterminate = false
        binding.undoTimer.max = TIMER_MAX
        binding.undoTimer.progress = TIMER_MAX
    }

    private fun startUndoTimer() {
        binding.undoTimer.isVisible = true
        binding.undoTimer.max = TIMER_MAX
        binding.undoTimer.progress = TIMER_MAX
        undoAnimator?.cancel()
        undoAnimator = ValueAnimator.ofInt(TIMER_MAX, 0).apply {
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

    private fun openTaskDetail(taskId: Long) {
        val action = IndexFragmentDirections.actionIndexFragmentToTaskFragment(taskId)
        findNavController().navigate(action)
    }

    private fun loadUserAvatar() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = requireContext().applicationContext
            val prefs = ctx.getSharedPreferences("app", Context.MODE_PRIVATE)
            val userId = prefs.getLong("remember_user_id", 1L)
            val url = withContext(Dispatchers.IO) {
                AppDatabase.get(ctx).userDao().getById(userId)?.avatar_url
            }
            Glide.with(this@IndexFragment)
                .load(url)
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .circleCrop()
                .into(binding.ivUser)
        }
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
    private fun dpInt(value: Float): Int = dp(value).toInt()

    override fun onDestroyView() {
        stopUndoTimer()
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TIMER_MAX = 1000
        private const val UNDO_DURATION = 4000L
    }
}