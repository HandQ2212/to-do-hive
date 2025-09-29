package com.proptit.todohive.ui.home.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.databinding.ItemTaskBinding

class TaskAdapter : ListAdapter<TaskWithCategory, TaskAdapter.TaskViewHolder>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TaskWithCategory>() {
            override fun areItemsTheSame(o: TaskWithCategory, n: TaskWithCategory) =
                o.task.task_id == n.task.task_id
            override fun areContentsTheSame(o: TaskWithCategory, n: TaskWithCategory) = o == n
        }
    }
    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskWithCategory) {
            binding.taskTitle = item.task.title
            binding.taskTime  = formatTime(item.task.even_at)
            binding.categoryName = item.category?.name ?: "None"
            binding.priorityText = "P ${item.task.priority}"
            binding.subCountText = " ${0} "
        }
        private fun formatTime(instant: java.time.Instant): String =
            java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                .toLocalTime().toString().substring(0,5)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inf = LayoutInflater.from(parent.context)
        val b = ItemTaskBinding.inflate(inf, parent, false)
        return TaskViewHolder(b)
    }
    override fun onBindViewHolder(h: TaskViewHolder, pos: Int) = h.bind(getItem(pos))
}