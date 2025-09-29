package com.proptit.todohive.ui.home.task
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proptit.todohive.R
import com.proptit.todohive.common.SpacesItemDecoration
import com.proptit.todohive.data.local.model.CategoryOption
import com.proptit.todohive.databinding.BottomsheetPickCategoryBinding
import com.proptit.todohive.ui.home.task.add.AddTaskSheetViewModel

class PickCategorySheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetPickCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTaskSheetViewModel by activityViewModels()

    private val adapter by lazy {
        CategoryOptionAdapter { option ->
            viewModel.setPickedCategoryId(option.id)
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
        val space = resources.getDimensionPixelSize(R.dimen.grid_space_8)
        binding.apply {
            tvTitle.text = getString(R.string.choose_category)
            rvCategories.layoutManager = GridLayoutManager(requireContext(), 3)
            rvCategories.adapter = adapter
            rvCategories.addItemDecoration(SpacesItemDecoration(space, space))
            btnAddCategory.setOnClickListener {
                // TODO: open CreateCategoryFragment
                dismiss()
            }
        }
        // TODO: load from DB/Repository
        adapter.submitList(
            listOf(
                CategoryOption(null, "None", Color.parseColor("#616161"), R.drawable.ic_tag),
                CategoryOption(1L, "University", Color.parseColor("#6C63FF"), R.drawable.ic_tag),
                CategoryOption(2L, "Home",       Color.parseColor("#EF5350"), R.drawable.ic_tag),
                CategoryOption(3L, "Work",       Color.parseColor("#FBC02D"), R.drawable.ic_tag),
                CategoryOption(4L, "Sport",      Color.parseColor("#76FF03"), R.drawable.ic_tag),
                CategoryOption(5L, "Design",     Color.parseColor("#64FFDA"), R.drawable.ic_tag),
                CategoryOption(6L, "Social",     Color.parseColor("#FF80AB"), R.drawable.ic_tag),
                CategoryOption(7L, "Music",      Color.parseColor("#E040FB"), R.drawable.ic_tag),
                CategoryOption(8L, "Health",     Color.parseColor("#69F0AE"), R.drawable.ic_tag),
                CategoryOption(9L, "Movie",      Color.parseColor("#82B1FF"), R.drawable.ic_tag),
                CategoryOption(10L,"Grocery",    Color.parseColor("#B2FF59"), R.drawable.ic_tag),
                CategoryOption(11L,"Create New", Color.parseColor("#69F0AE"), R.drawable.ic_add)
            )
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}