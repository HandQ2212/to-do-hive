package com.proptit.todohive.ui.home.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.proptit.todohive.R
import com.proptit.todohive.databinding.FragmentIndexBinding

class IndexFragment : Fragment() {

    private var _binding: FragmentIndexBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TasksViewModel by viewModels {
        TasksViewModelFactory(requireContext().applicationContext)
    }
    private val adapter = TaskAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIndexBinding.inflate(inflater, container, false)
        binding.viewModel = taskViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        taskViewModel.filteredTasks.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding.tvFilter.setOnClickListener { anchor ->
            PopupMenu(requireContext(), anchor).apply {
                menuInflater.inflate(R.menu.menu_filter, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_today     -> taskViewModel.setFilter(Filter.TODAY)
                        R.id.action_tomorrow  -> taskViewModel.setFilter(Filter.TOMORROW)
                        R.id.action_yesterday -> taskViewModel.setFilter(Filter.YESTERDAY)
                        R.id.action_completed -> taskViewModel.setFilter(Filter.COMPLETED)
                        else -> return@setOnMenuItemClickListener false
                    }; true
                }
            }.show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}