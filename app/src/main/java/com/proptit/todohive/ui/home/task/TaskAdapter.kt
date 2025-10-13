package com.proptit.todohive.ui.home.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.databinding.ItemTaskBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TaskAdapter(
    private val onToggleDone: (TaskEntity) -> Unit,
    private val onClick: (TaskEntity) -> Unit
) : ListAdapter<TaskWithCategory, TaskAdapter.TaskViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TaskWithCategory>() {
            override fun areItemsTheSame(old: TaskWithCategory, new: TaskWithCategory) =
                old.task.task_id == new.task.task_id

            override fun areContentsTheSame(old: TaskWithCategory, new: TaskWithCategory) =
                old == new
        }
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskWithCategory) = with(binding) {
            task = item.task
            taskTitle = item.task.title
            taskTime = formatTime(item.task.even_at)
            category = item.category
            priorityText = "P${item.task.priority}"
            subCountText = "0"
            onToggleDone = this@TaskAdapter.onToggleDone
            tvTitle.isSelected = true
            executePendingBindings()

            root.setOnClickListener {
                onClick.invoke(item.task)
            }
        }

        private fun formatTime(instant: java.time.Instant): String {
            val zone = ZoneId.systemDefault()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            return formatter.format(instant.atZone(zone))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}