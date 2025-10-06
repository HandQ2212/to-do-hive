package com.proptit.todohive.ui.home.task

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.R
import com.proptit.todohive.common.SpacesItemDecoration
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.databinding.BottomsheetPickCategoryBinding
import com.proptit.todohive.ui.home.TaskFragment
import com.proptit.todohive.ui.home.task.add.AddTaskSheetViewModel

class PickCategorySheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetPickCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val addTaskViewModel: AddTaskSheetViewModel by activityViewModels()

    private val categoryAdapter by lazy {
        CategoryAdapter { category: CategoryEntity ->
            addTaskViewModel.setPickedCategoryId(category.category_id)
            onCategoryPicked(category.category_id, category.name)
        }
    }

    private val createCategoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val newId = data?.getLongExtra(CreateCategoryActivity.EXTRA_CATEGORY_ID, -1L) ?: -1L
            val newName = data?.getStringExtra(CreateCategoryActivity.EXTRA_CATEGORY_NAME) ?: ""
            if (newId > 0L) {
                addTaskViewModel.setPickedCategoryId(newId)
                onCategoryPicked(newId, newName)
            }
        }
    }

    private fun onCategoryPicked(id: Long?, name: String?) {
        parentFragmentManager.setFragmentResult(
            TaskFragment.REQ_CATEGORY,
            bundleOf(
                TaskFragment.RES_CATEGORY_ID to (id ?: -1L),
                TaskFragment.RES_CATEGORY_NAME to (name ?: "")
            )
        )
        dismiss()
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
        rvCategories.adapter = categoryAdapter
        val space = resources.getDimensionPixelSize(R.dimen.grid_space_8)
        if (rvCategories.itemDecorationCount == 0) {
            rvCategories.addItemDecoration(SpacesItemDecoration(space, space))
        }
    }

    private fun observeCategories() {
        categoryViewModel.filtered.observe(viewLifecycleOwner) { list ->
            categoryAdapter.submitList(list)
        }
    }

    private fun setupActions() {
        binding.btnAddCategory.setOnClickListener {
            val intent = CreateCategoryActivity.createIntent(requireContext())
            createCategoryLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}