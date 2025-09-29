package com.proptit.todohive.ui.home.task

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.R
import com.proptit.todohive.common.SpacesItemDecoration
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.databinding.BottomsheetPickCategoryBinding
import com.proptit.todohive.ui.home.CreateCategoriesFragment
import com.proptit.todohive.ui.home.task.add.AddTaskSheetViewModel

class PickCategorySheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetPickCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val addTaskViewModel: AddTaskSheetViewModel by activityViewModels()

    private val adapter by lazy {
        CategoryAdapter { cat: CategoryEntity ->
            addTaskViewModel.setPickedCategoryId(cat.category_id)
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetPickCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecycler()
        observeCategories()
        setupActions()
    }

    private fun setupRecycler() = with(binding) {
        tvTitle.text = getString(R.string.choose_category)
        val span = 3
        rvCategories.layoutManager = GridLayoutManager(requireContext(), span)
        rvCategories.adapter = adapter

        val space = resources.getDimensionPixelSize(R.dimen.grid_space_8)
        if (rvCategories.itemDecorationCount == 0) {
            rvCategories.addItemDecoration(SpacesItemDecoration(space, space))
        }
    }

    private fun observeCategories() {
        categoryViewModel.filtered.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    private fun setupActions() = with(binding) {
        btnAddCategory.setOnClickListener {
            startActivity(Intent(requireContext(), CreateCategoriesFragment::class.java))
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
