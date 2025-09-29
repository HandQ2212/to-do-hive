package com.proptit.todohive.ui.home.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.databinding.ItemCategoryOptionBinding

class CategoryAdapter(
    private val onClick: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryAdapter.CategoryViewHolder>(DIFF) {

    init { setHasStableIds(true) }
    override fun getItemId(position: Int) = getItem(position).category_id

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryEntity>() {
            override fun areItemsTheSame(old: CategoryEntity, new: CategoryEntity) =
                old.category_id == new.category_id
            override fun areContentsTheSame(old: CategoryEntity, new: CategoryEntity) =
                old == new
        }
    }

    inner class CategoryViewHolder(val b: ItemCategoryOptionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: CategoryEntity) = with(b) {
            category = item
            onClick = this@CategoryAdapter.onClick
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return CategoryViewHolder(ItemCategoryOptionBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) = holder.bind(getItem(position))
}