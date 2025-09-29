package com.proptit.todohive.ui.home.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.proptit.todohive.databinding.BottomsheetPickTimeBinding
import com.proptit.todohive.ui.home.task.add.AddTaskSheetViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PickTimeSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetPickTimeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTaskSheetViewModel by activityViewModels()

    private var pickedDate: LocalDate? = null
    private var pickedTime: LocalTime? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetPickTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.pickedInstant.value?.let { instant ->
            val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            pickedDate = zonedDateTime.toLocalDate()
            pickedTime = zonedDateTime.toLocalTime()
            binding.tvSummary.text = zonedDateTime.toString()
        }

        viewModel.pickedInstant.observe(viewLifecycleOwner) { instant ->
            instant?.let {
                val zonedDateTime = ZonedDateTime.ofInstant(it, ZoneId.systemDefault())
                binding.tvSummary.text = zonedDateTime.toString()
            }
        }

        binding.btnPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pick date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            picker.addOnPositiveButtonClickListener { millis ->
                pickedDate = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                updateSummary()
            }
            picker.show(parentFragmentManager, "date_picker")
        }

        binding.btnPickTime.setOnClickListener {
            val now = pickedTime ?: LocalTime.now()
            val picker = MaterialTimePicker.Builder()
                .setTitleText("Pick time")
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(now.hour)
                .setMinute(now.minute)
                .build()
            picker.addOnPositiveButtonClickListener {
                pickedTime = LocalTime.of(picker.hour, picker.minute)
                updateSummary()
            }
            picker.show(parentFragmentManager, "time_picker")
        }

        binding.btnConfirm.setOnClickListener {
            val d = pickedDate ?: LocalDate.now()
            val t = pickedTime ?: LocalTime.of(9, 0)
            val instant = ZonedDateTime.of(d, t, ZoneId.systemDefault()).toInstant()
            viewModel.setPickedInstant(instant)
            dismiss()
        }
    }

    private fun updateSummary() {
        val date = pickedDate
        val time = pickedTime
        if (date != null && time != null) {
            val zonedDateTime = ZonedDateTime.of(date, time, ZoneId.systemDefault())
            binding.tvSummary.text = zonedDateTime.toString()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}