package com.proptit.todohive.ui.home.task

import com.proptit.todohive.data.local.model.CategoryOption
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proptit.todohive.databinding.ItemCategoryOptionBinding

class CategoryOptionAdapter(
    private val onClick: (CategoryOption) -> Unit
) : ListAdapter<CategoryOption, CategoryOptionAdapter.CategoryOptionViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryOption>() {
            override fun areItemsTheSame(old: CategoryOption, new: CategoryOption) =
                old.id == new.id
            override fun areContentsTheSame(old: CategoryOption, new: CategoryOption) =
                old == new
        }
    }

    inner class CategoryOptionViewHolder(private val binding: ItemCategoryOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryOption) = with(binding) {
            tvName.text = item.name
            iconContainer.setCardBackgroundColor(item.color)
            ivIcon.setImageResource(item.iconRes)
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryOptionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCategoryOptionBinding.inflate(inflater, parent, false)
        return CategoryOptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryOptionViewHolder, position: Int) =
        holder.bind(getItem(position))
}