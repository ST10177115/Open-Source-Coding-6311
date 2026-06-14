package com.budgettracker.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityMonthlySummaryBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import java.util.Calendar

class MonthlySummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonthlySummaryBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Monthly Summary"

        loadMonthlySummary()
    }

    private fun loadMonthlySummary() {
        val userId = sessionManager.getUserId()
        val calendar = Calendar.getInstance()

        // Show last 6 months
        val months = mutableListOf<Pair<String, String>>()
        for (i in 0..5) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val startDate = "%04d-%02d-01".format(year, month)
            val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val endDate = "%04d-%02d-%02d".format(year, month, lastDay)
            months.add(Pair(startDate, endDate))
            calendar.add(Calendar.MONTH, -1)
        }

        loadMonthData(userId, months, 0)
    }

    private fun loadMonthData(
        userId: Int,
        months: List<Pair<String, String>>,
        index: Int
    ) {
        if (index >= months.size) return

        val (startDate, endDate) = months[index]

        db.expenseDao().getTotalSpentForPeriod(userId, startDate, endDate)
            .observe(this) { total ->
                val spent = total ?: 0.0
                addMonthCard(startDate, spent)
                if (index + 1 < months.size) {
                    loadMonthData(userId, months, index + 1)
                }
            }
    }

    private fun addMonthCard(startDate: String, spent: Double) {
        val parts = startDate.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()

        val monthNames = listOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )
        val monthName = "${monthNames[month - 1]} $year"

        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 0, 0, 16) }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.WHITE)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)
        }

        val monthText = TextView(this).apply {
            text = monthName
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#212121"))
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val amountText = TextView(this).apply {
            text = DateUtils.formatCurrency(spent)
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(
                if (spent > 0) Color.parseColor("#E53935")
                else Color.parseColor("#9E9E9E")
            )
        }

        container.addView(monthText)
        container.addView(amountText)
        card.addView(container)
        binding.summaryContainer.addView(card)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}