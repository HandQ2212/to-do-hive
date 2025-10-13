package com.proptit.todohive.ui.home.bin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import com.proptit.todohive.R
import com.proptit.todohive.common.SwipeToRestoreDeleteCallback
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.databinding.FragmentBinBinding
import kotlinx.coroutines.launch

class BinFragment : Fragment(R.layout.fragment_bin) {

    private var _binding: FragmentBinBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskBinViewModel by viewModels {
        TaskBinViewModel.Factory(requireContext().applicationContext)
    }

    private val adapter by lazy { TaskBinAdapter { task -> openTaskDetail(task.task_id) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentBinBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        viewModel.deletedTasks.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.isVisible = list.isNullOrEmpty()
        }
        setupActions()
        attachSwipeGestures()
    }

    private fun attachSwipeGestures() {
        val swipe = SwipeToRestoreDeleteCallback(
            context = requireContext(),
            onSwipedLeft = { viewHolder ->        
                val pos = viewHolder.bindingAdapterPosition
                val task = adapter.currentList.getOrNull(pos)?.task
                if (task == null) { adapter.notifyItemChanged(pos); return@SwipeToRestoreDeleteCallback }
                confirmDeleteForever(task, pos)
            },
            onSwipedRight = { viewHolder ->        
                val pos = viewHolder.bindingAdapterPosition
                val task = adapter.currentList.getOrNull(pos)?.task
                if (task == null) { adapter.notifyItemChanged(pos); return@SwipeToRestoreDeleteCallback }
                restoreTask(task, pos)
            }
        )
        ItemTouchHelper(swipe).attachToRecyclerView(binding.rvTasks)
    }
    private fun restoreTask(task: TaskEntity, position: Int) {
        lifecycleScope.launch { viewModel.onSwipeRestore(task) }
        val snack = createBaseSnackbar(getString(R.string.restored_fmt, task.title))
            .setAction(R.string.undo) { viewModel.moveToBin(task.task_id) }
        customizeSnackbar(snack)
        snack.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(tb: Snackbar?, event: Int) {
                adapter.notifyItemChanged(position) 
            }
        })
        snack.show()
    }
    private fun confirmDeleteForever(task: TaskEntity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_forever)
            .setMessage(getString(R.string.delete_forever_fmt, task.title))
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                adapter.notifyItemChanged(position)
            }
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch { viewModel.onSwipeDeleteForever(task) }
                val snack = createBaseSnackbar(getString(R.string.deleted_permanently))
                customizeSnackbar(snack)
                snack.show()
            }
            .setOnCancelListener { adapter.notifyItemChanged(position) }
            .show()
    }

    private fun openTaskDetail(taskId: Long) {
        val action = BinFragmentDirections.actionBinFragmentToTaskFragment(taskId)
        findNavController().navigate(action)
    }
    private fun createBaseSnackbar(message: String) =
        Snackbar
            .make(requireActivity().findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
            .setAnchorView(requireActivity().findViewById(R.id.fabAdd))
            .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)

    private fun customizeSnackbar(snack: Snackbar) {
        val ctx = requireContext()
        val view = snack.view
        view.background = MaterialShapeDrawable(
            ShapeAppearanceModel().toBuilder().setAllCornerSizes(dp(12f)).build()
        ).apply {
            fillColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(ctx, R.color.gray)
            )
            elevation = dp(6f)
        }
        (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
            val m = dpInt(12f); lp.setMargins(m, m, m, m); view.layoutParams = lp
        }
        val tv = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        val btn = view.findViewById<Button>(com.google.android.material.R.id.snackbar_action)
        tv.setTextColor(ContextCompat.getColor(ctx, R.color.white))
        btn.setTextColor(ContextCompat.getColor(ctx, R.color.warning))
        ContextCompat.getDrawable(ctx, R.drawable.ic_warning)?.mutate()?.let { d ->
            DrawableCompat.setTint(d, ContextCompat.getColor(ctx, R.color.warning))
            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null)
            tv.compoundDrawablePadding = dpInt(8f)
            tv.maxLines = 3
        }
    }

    private fun setupActions() {
        binding.btnRestoreAll.setOnClickListener {
            if (adapter.currentList.isEmpty()) {
                toastSnack(getString(R.string.nothing_to_restore))
            } else {
                viewModel.restoreAll()
                toastSnack(getString(R.string.all_restored))
            }
        }

        binding.btnClearAll.setOnClickListener {
            if (adapter.currentList.isEmpty()) {
                toastSnack(getString(R.string.nothing_to_clear))
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_forever)
                    .setMessage(getString(R.string.delete_all_permanently_confirm))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewModel.clearAll()
                        toastSnack(getString(R.string.deleted_permanently))
                    }
                    .show()
            }
        }
    }

    private fun toastSnack(msg: String) {
        Snackbar.make(requireActivity().findViewById(R.id.main), msg, Snackbar.LENGTH_SHORT)
            .setAnchorView(requireActivity().findViewById(R.id.fabAdd))
            .show()
    }


    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun dpInt(v: Float) = dp(v).toInt()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}