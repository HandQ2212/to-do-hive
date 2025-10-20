package com.proptit.todohive.ui.home.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proptit.todohive.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class WeekDayAdapter(
    private val onClick: (LocalDate) -> Unit
) : ListAdapter<LocalDate, WeekDayAdapter.DayViewHolder>(Diff) {

    private var selected: LocalDate? = null
    private var datesWithTasks: Set<LocalDate> = emptySet()


    fun setSelected(date: LocalDate?) {
        selected = date
        notifyDataSetChanged()
    }
    fun setTaskDates(dates: Set<LocalDate>) {
        datesWithTasks = dates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_week_day, parent, false)
        return DayViewHolder(v)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = getItem(position)
        val isSelected = date == selected
        val hasTasks = datesWithTasks.contains(date)
        holder.bind(date, isSelected, hasTasks)
        holder.itemView.setOnClickListener {
            setSelected(date)
            onClick(date)
        }
    }

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDay: TextView = view.findViewById(R.id.tvDay)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val container: View = view.findViewById(R.id.container)
        private val taskDot: View = view.findViewById(R.id.taskDot)

        fun bind(date: LocalDate, isSelected: Boolean, hasTasks: Boolean) {
            val ctx = itemView.context
            val locale = Locale.getDefault()

            tvDay.text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
            tvDate.text = date.dayOfMonth.toString()

            val dayColor = when (date.dayOfWeek) {
                DayOfWeek.SUNDAY, DayOfWeek.SATURDAY -> R.color.red
                else -> R.color.white
            }

            tvDay.setTextColor(ContextCompat.getColor(ctx, dayColor))
            tvDate.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))

            container.background = ContextCompat.getDrawable(
                ctx,
                if (isSelected) R.drawable.bg_weekday_selected else R.drawable.bg_weekday_normal
            )

            taskDot.visibility = if (hasTasks) View.VISIBLE else View.GONE
        }
    }

    private object Diff : DiffUtil.ItemCallback<LocalDate>() {
        override fun areItemsTheSame(old: LocalDate, new: LocalDate) = old == new
        override fun areContentsTheSame(old: LocalDate, new: LocalDate) = old == new
    }
}
