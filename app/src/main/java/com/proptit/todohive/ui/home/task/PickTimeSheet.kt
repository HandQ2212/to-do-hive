package com.proptit.todohive.ui.home.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.proptit.todohive.databinding.BottomsheetPickTimeBinding
import com.proptit.todohive.ui.home.TaskFragment
import com.proptit.todohive.ui.home.task.add.AddTaskSheetViewModel
import java.time.*

class PickTimeSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetPickTimeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTaskSheetViewModel by activityViewModels()

    private var pickedDate: LocalDate? = null
    private var pickedTime: LocalTime? = null

    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val DEFAULT_TIME = LocalTime.of(9, 0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetPickTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun onTimePicked(epochMillis: Long) {
        parentFragmentManager.setFragmentResult(
            TaskFragment.REQ_TIME,
            bundleOf(TaskFragment.RES_TIME_MS to epochMillis)
        )
        dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.pickedInstant.value?.let { instant ->
            val zdt = instant.atZone(zone)
            pickedDate = zdt.toLocalDate()
            pickedTime = zdt.toLocalTime()
        }
        updateUiSummary()

        binding.chipToday.setOnClickListener {
            val today = LocalDate.now(zone)
            pickedDate = today
            if (pickedTime == null) pickedTime = DEFAULT_TIME
            updateUiSummary()
        }
        binding.chipTomorrow.setOnClickListener {
            val d = LocalDate.now(zone).plusDays(1)
            pickedDate = d
            if (pickedTime == null) pickedTime = DEFAULT_TIME
            updateUiSummary()
        }
        binding.chipNextWeek.setOnClickListener {
            val d = LocalDate.now(zone).plusWeeks(1)
            pickedDate = d
            if (pickedTime == null) pickedTime = DEFAULT_TIME
            updateUiSummary()
        }
        binding.chipClear.setOnClickListener {
            pickedDate = null
            pickedTime = null
            updateUiSummary()
        }

        binding.cardPickDate.setOnClickListener { openDatePicker() }
        binding.cardPickTime.setOnClickListener { openTimePicker() }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener { onConfirm() }
    }

    private fun openDatePicker() {
        val initialDate = pickedDate ?: viewModel.pickedInstant.value?.atZone(zone)?.toLocalDate() ?: LocalDate.now(zone)
        val selection = initialDate.atStartOfDay(zone).toInstant().toEpochMilli()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pick date")
            .setSelection(selection)
            .build()

        picker.addOnPositiveButtonClickListener { utcMillis ->
            pickedDate = Instant.ofEpochMilli(utcMillis).atZone(zone).toLocalDate()
            updateUiSummary()
        }
        picker.show(parentFragmentManager, "date_picker")
    }

    private fun openTimePicker() {
        val seed = pickedTime ?: viewModel.pickedInstant.value?.atZone(zone)?.toLocalTime() ?: LocalTime.now(zone)
        val picker = MaterialTimePicker.Builder()
            .setTitleText("Pick time")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(seed.hour)
            .setMinute(seed.minute)
            .build()

        picker.addOnPositiveButtonClickListener {
            pickedTime = LocalTime.of(picker.hour, picker.minute)
            updateUiSummary()
        }
        picker.show(parentFragmentManager, "time_picker")
    }

    private fun onConfirm() {
        if (pickedDate == null && pickedTime == null) {
            viewModel.clearPickedInstant()
            dismiss()
            return
        }
        val d = pickedDate ?: LocalDate.now(zone)
        val t = pickedTime ?: DEFAULT_TIME
        val instant = ZonedDateTime.of(d, t, zone).toInstant()
        viewModel.setPickedInstant(instant)
        parentFragmentManager.setFragmentResult(
            TaskFragment.REQ_TIME,
            bundleOf(TaskFragment.RES_TIME_MS to instant.toEpochMilli())
        )

        dismiss()
    }

    private fun updateUiSummary() {
        val date = pickedDate
        val time = pickedTime

        when {
            date == null && time == null -> {
                binding.summary  = "No time selected"
                binding.dateText = "—"
                binding.timeText = "—"
            }
            date != null && time != null -> {
                val zdt = ZonedDateTime.of(date, time, zone)
                binding.summary  = TimeFmt.full(zdt)
                binding.dateText = TimeFmt.date(zdt.toLocalDate())
                binding.timeText = TimeFmt.time(zdt.toLocalTime())
            }
            date != null -> {
                binding.summary  = "On ${TimeFmt.date(date)} (no time)"
                binding.dateText = TimeFmt.date(date)
                binding.timeText = "—"
            }
            else -> {
                time?.let { binding.summary  = "At ${TimeFmt.time(it)} (today)" }
                binding.dateText = TimeFmt.date(LocalDate.now(zone))
                binding.timeText = TimeFmt.time(time!!)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
