package com.proptit.todohive.ui.onboarding

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.proptit.todohive.R
import com.proptit.todohive.data.local.model.OnboardingPage
import com.proptit.todohive.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val pages = listOf(
        OnboardingPage(
            R.drawable.ill_manage, "Manage your tasks",
            "You can easily manage all of your daily tasks in ToDoHive for free"
        ),
        OnboardingPage(
            R.drawable.ill_routine, "Create daily routine",
            "Create your personalized routine to stay productive"
        ),
        OnboardingPage(
            R.drawable.ill_organize, "Organize your tasks",
            "Group your daily tasks into separate categories"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupPager()
        styleNextButton()
        initIndicatorsAndTexts()
        registerPageChangeCallback()
        setupClickListeners()
    }

    private fun setupPager() {
        val adapter = OnboardingAdapter(pages)
        binding.pager.adapter = adapter
    }

    private fun styleNextButton() {
        binding.btnNext.apply {
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            strokeWidth = 2
            cornerRadius = 24
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.purple))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun initIndicatorsAndTexts() {
        updateBars(0)
        updateTexts(0)
    }

    private fun registerPageChangeCallback() {
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateBars(position)
                updateTexts(position)
                binding.btnNext.text = if (position == pages.lastIndex) "GET STARTED" else "NEXT"
                binding.btnBack.alpha = if (position == 0) 0.5f else 1f
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            if (binding.pager.currentItem < pages.lastIndex) {
                binding.pager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
        binding.btnSkipTop.setOnClickListener { finishOnboarding() }
        binding.btnBack.setOnClickListener {
            if (binding.pager.currentItem > 0) binding.pager.currentItem -= 1
        }
    }

    private fun updateTexts(position: Int) {
        val page = pages[position]
        binding.textTitle.text = page.title
        binding.textDescription.text = page.description
    }

    private fun updateBars(activeIndex: Int) {
        val active = ContextCompat.getDrawable(requireContext(), R.drawable.indicator_bar_active)
        val inactive = ContextCompat.getDrawable(requireContext(), R.drawable.indicator_bar_inactive)
        listOf(binding.bar1, binding.bar2, binding.bar3)
            .forEachIndexed { i, v -> v.background = if (i == activeIndex) active else inactive }
    }

    private fun finishOnboarding() {
        requireContext().getSharedPreferences("app", Context.MODE_PRIVATE)
            .edit().putBoolean("onboarding_done", true).apply()

        findNavController().navigate(
            R.id.action_onboarding_to_start,
            null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}