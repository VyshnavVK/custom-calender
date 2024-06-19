package com.vyshnav.custom_calender

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vyshnav.custom_calender.databinding.ActivityMainBinding
import com.vyshnav.custom_calender.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var selectedDate : LocalDate?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initUI()
    }

    private fun calculateDaysDifference(startDate: Calendar, endDate: Calendar): Long {
        val startTimeInMillis = startDate.timeInMillis
        val endTimeInMillis = endDate.timeInMillis

        return TimeUnit.MILLISECONDS.toDays(endTimeInMillis - startTimeInMillis)
    }

    private fun initUI() {
        val calendar = Calendar.getInstance()
        val lastDayOfYear = Calendar.getInstance().apply { set(calendar.get(Calendar.YEAR) + 1, Calendar.JANUARY, 1) }
        val MAX_SELECTABLE_DATE_IN_FUTURE = calculateDaysDifference(calendar, lastDayOfYear)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val tvDay = CalendarDayLayoutBinding.bind(view).calendarDayText
            val tvQar = CalendarDayLayoutBinding.bind(view).qarText
            val clCalendar = CalendarDayLayoutBinding.bind(view).clCalender
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(0)
        val endMonth = YearMonth.of(currentMonth.year, 12)
        val daysOfWeek = daysOfWeek()
        binding.calendarView.setup(startMonth, endMonth, DayOfWeek.SUNDAY)
        binding.calendarView.scrollToMonth(currentMonth)

        val disabledDates = listOf(
            LocalDate.of(2024, 6, 17),
            LocalDate.of(2024, 7, 4)
        ).toMutableList()


        val today = Calendar.getInstance(Locale.US)
        val currentDate = LocalDate.now()
        for (i in 0 until MAX_SELECTABLE_DATE_IN_FUTURE) {
            val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

            if (dayOfWeek == Calendar.MONDAY) {
                disabledDates.add(
                    LocalDate.of(
                        today.get(Calendar.YEAR),
                        addLeadingZero(today.get(Calendar.MONTH) + 1).toInt(),
                        addLeadingZero(today.get(Calendar.DAY_OF_MONTH)).toInt()
                    )
                )
            }

            today.add(Calendar.DATE, 1)
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            @SuppressLint("SuspiciousIndentation")
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.tvDay.text = data.date.dayOfMonth.toString()

                if (data.date == selectedDate) {
                    container.clCalendar.setBackgroundResource(R.drawable.selected_date_bg)
                    container.tvDay.setTextColor(Color.WHITE)
                    container.tvQar.setTextColor(Color.WHITE)
                } else {
                    container.clCalendar.background = null
                    container.tvDay.setTextColor(Color.BLACK)
                    container.tvQar.setTextColor(Color.parseColor("#9F3090"))
                }

                if (data.date.dayOfWeek == DayOfWeek.MONDAY) {
                    container.clCalendar.setBackgroundResource(R.drawable.monday_disabled_bg)
                }

                if (data.date == currentDate) {
                    container.clCalendar.setBackgroundResource(R.drawable.current_date_bg)
                    container.tvDay.setTextColor(Color.WHITE)
                    container.tvQar.setTextColor(Color.WHITE)
                }

                if (data.date in disabledDates || data.date.isBefore(currentDate)) {
                    container.tvDay.setTextColor(Color.LTGRAY)
                    container.tvDay.isEnabled = false
                    container.tvQar.visibility = View.GONE
                    container.clCalendar.setOnClickListener(null)
                } else {
                    container.tvDay.isEnabled = true
                    container.tvQar.visibility = View.VISIBLE
                    container.tvQar.setTextColor(Color.parseColor("#9F3090"))  // Reset tvQar color
                    container.clCalendar.setOnClickListener {
                        selectedDate = data.date
                        container.tvDay.setTextColor(Color.WHITE)
                        container.tvQar.setTextColor(Color.WHITE)
                        binding.calendarView.notifyCalendarChanged()

                        Toast.makeText(
                            this@MainActivity,
                            "Selected: ${data.date}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                if (data.position != DayPosition.MonthDate) {
                    container.tvDay.setTextColor(Color.LTGRAY)
                    container.tvQar.setTextColor(Color.LTGRAY)
                    container.clCalendar.setOnClickListener(null)
                }
            }
        }

        binding.calendarView.monthScrollListener = { month ->
            updateMonthText(month.yearMonth)
        }
    }

    private fun updateMonthText(yearMonth: YearMonth) {
        val formatterMonth = DateTimeFormatter.ofPattern("MMMM", Locale.US)
        val formatterYear = DateTimeFormatter.ofPattern("yyyy", Locale.US)
        binding.tvMonth.text = yearMonth.format(formatterMonth)
        binding.tvYear.text = yearMonth.format(formatterYear)

    }

    fun addLeadingZero(number: Int): String = String.format(Locale.UK, "%02d", number)
}


