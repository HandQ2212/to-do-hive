package com.proptit.todohive.ui.home.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.proptit.todohive.R
import com.proptit.todohive.databinding.ItemPriorityOptionBinding

class PriorityAdapter(
    private val onPick: (Int) -> Unit
) : ListAdapter<Int, PriorityAdapter.PriorityViewHolder>(DIFF) {

    var selectedValue: Int? = null
        private set

    fun setSelected(value: Int?) {
        val old = selectedValue
        selectedValue = value
        if (old != value) notifyDataSetChanged()
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(old: Int, new: Int) = old == new
            override fun areContentsTheSame(old: Int, new: Int) = old == new
        }
    }

    inner class PriorityViewHolder(val binding: ItemPriorityOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(value: Int) = with(binding) {
            this.value = value
            val isSel = value == selectedValue
            this.isSelected = isSel
            this.onClick = { v ->
                onPick(v)
                setSelected(v)
            }
            styleCard(root as MaterialCardView, isSel)
            executePendingBindings()
        }

        private fun styleCard(card: MaterialCardView, isSel: Boolean) {
            val ctx = card.context
            val purple = ContextCompat.getColor(ctx, R.color.purple)
            val dark   = 0xFF1E1E1E.toInt()
            val stroke = 0xFF333333.toInt()

            card.setCardBackgroundColor(if (isSel) purple else dark)
            card.strokeColor = if (isSel) purple else stroke

            binding.ivFlag.setColorFilter(
                ContextCompat.getColor(ctx, if (isSel) android.R.color.white else R.color.gray)
            )
            binding.tvLabel.setTextColor(
                ContextCompat.getColor(ctx, if (isSel) android.R.color.white else android.R.color.white)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriorityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PriorityViewHolder(ItemPriorityOptionBinding.inflate(inflater, parent, false))
    }
    override fun onBindViewHolder(holder: PriorityViewHolder, position: Int) = holder.bind(getItem(position))
}
