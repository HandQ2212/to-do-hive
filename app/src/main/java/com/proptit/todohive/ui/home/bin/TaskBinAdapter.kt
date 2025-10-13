package com.proptit.todohive.ui.home.bin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.databinding.ItemTaskBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TaskBinAdapter(
    private val onClick: (TaskEntity) -> Unit
) : ListAdapter<TaskWithCategory, TaskBinAdapter.TaskBinViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TaskWithCategory>() {
            override fun areItemsTheSame(old: TaskWithCategory, new: TaskWithCategory) =
                old.task.task_id == new.task.task_id

            override fun areContentsTheSame(old: TaskWithCategory, new: TaskWithCategory) =
                old == new
        }
    }

    inner class TaskBinViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskWithCategory) = with(binding) {
            task = item.task
            taskTitle = item.task.title
            taskTime = formatTime(item.task.even_at)
            category = item.category
            priorityText = "P${item.task.priority}"
            subCountText = "0"

            onToggleDone = { /* no-op */ }
            ivCheck.visibility = View.GONE

            tvTitle.isSelected = true
            executePendingBindings()

            root.setOnClickListener { onClick.invoke(item.task) }
        }

        private fun formatTime(instant: java.time.Instant): String {
            val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
            return formatter.format(instant.atZone(ZoneId.systemDefault()))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskBinViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return TaskBinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskBinViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
