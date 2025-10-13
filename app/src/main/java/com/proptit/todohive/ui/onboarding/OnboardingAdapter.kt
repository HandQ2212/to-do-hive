package com.proptit.todohive.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.proptit.todohive.data.local.model.OnboardingPage
import com.proptit.todohive.databinding.ItemOnboardingPageBinding

class OnboardingAdapter(
    private val onboardingPages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(
        val binding: ItemOnboardingPageBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        return OnboardingViewHolder(inflateBinding(parent))
    }

    private fun inflateBinding(parent: ViewGroup): ItemOnboardingPageBinding {
        val inflater = LayoutInflater.from(parent.context)
        return ItemOnboardingPageBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val page = onboardingPages[position]
        bindPage(holder, page)
    }

    private fun bindPage(holder: OnboardingViewHolder, page: OnboardingPage) {
        holder.binding.img.setImageResource(page.imageResId)
    }

    override fun getItemCount(): Int {
        return onboardingPages.size
    }
}