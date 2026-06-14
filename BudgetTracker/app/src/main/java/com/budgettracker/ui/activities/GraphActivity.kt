package com.budgettracker.ui.activities

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityGraphBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    private var startDate = DateUtils.getFirstDayOfMonthDbString()
    private var endDate = DateUtils.getTodayDbString()

    private var minGoal = 0.0
    private var maxGoal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Spending Graph"

        updateDateButtons()

        db.userDao().getUserById(sessionManager.getUserId()).observe(this) { user ->
            user?.let {
                minGoal = it.minMonthlyGoal
                maxGoal = it.maxMonthlyGoal
                binding.tvGoalMin.text = "Min Goal: ${DateUtils.formatCurrency(minGoal)}"
                binding.tvGoalMax.text = "Max Goal: ${DateUtils.formatCurrency(maxGoal)}"
            }
            loadGraphData()
        }

        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }
    }

    private fun showDatePicker(isStart: Boolean) {
        val date = if (isStart) startDate else endDate
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val day = parts[2].toInt()

        DatePickerDialog(this, { _, y, m, d ->
            val selected = "%04d-%02d-%02d".format(y, m + 1, d)
            if (isStart) {
                if (selected <= endDate) {
                    startDate = selected
                    updateDateButtons()
                    loadGraphData()
                }
            } else {
                if (selected >= startDate) {
                    endDate = selected
                    updateDateButtons()
                    loadGraphData()
                }
            }
        }, year, month, day).show()
    }

    private fun updateDateButtons() {
        binding.btnStartDate.text = "From: ${DateUtils.dbStringToDisplay(startDate)}"
        binding.btnEndDate.text = "To: ${DateUtils.dbStringToDisplay(endDate)}"
    }

    private fun loadGraphData() {
        val userId = sessionManager.getUserId()

        db.expenseDao().getCategoryTotalsForPeriod(userId, startDate, endDate)
            .observe(this) { totals ->
                val entries = ArrayList<BarEntry>()
                val labels = ArrayList<String>()

                totals.forEachIndexed { index, total ->
                    entries.add(BarEntry(index.toFloat(), total.totalAmount.toFloat()))
                    labels.add(total.categoryName ?: "Unknown")
                }

                val dataSet = BarDataSet(entries, "Amount Spent").apply {
                    colors = listOf(
                        Color.parseColor("#4CAF50"),
                        Color.parseColor("#2196F3"),
                        Color.parseColor("#FF9800"),
                        Color.parseColor("#E91E63"),
                        Color.parseColor("#9C27B0"),
                        Color.parseColor("#00BCD4")
                    )
                    valueTextSize = 10f
                    valueTextColor = Color.BLACK
                }

                binding.barChart.apply {
                    data = BarData(dataSet)
                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    xAxis.granularity = 1f
                    xAxis.labelRotationAngle = -30f
                    axisRight.isEnabled = false
                    description.isEnabled = false
                    legend.isEnabled = true

                    axisLeft.removeAllLimitLines()
                    if (maxGoal > 0) {
                        val minLine = LimitLine(minGoal.toFloat(), "Min Goal").apply {
                            lineColor = Color.BLUE
                            lineWidth = 2f
                            textColor = Color.BLUE
                            textSize = 10f
                        }
                        val maxLine = LimitLine(maxGoal.toFloat(), "Max Goal").apply {
                            lineColor = Color.RED
                            lineWidth = 2f
                            textColor = Color.RED
                            textSize = 10f
                        }
                        axisLeft.addLimitLine(minLine)
                        axisLeft.addLimitLine(maxLine)
                    }

                    animateY(1000)
                    invalidate()
                }
            }

        db.expenseDao().getTotalSpentForPeriod(userId, startDate, endDate)
            .observe(this) { total ->
                val spent = total ?: 0.0
                binding.tvTotalSpent.text = "Total Spent: ${DateUtils.formatCurrency(spent)}"

                if (maxGoal > 0) {
                    val progress = ((spent / maxGoal) * 100).toInt().coerceIn(0, 100)
                    binding.progressGoal.progress = progress

                    binding.tvGoalStatus.text = when {
                        spent < minGoal -> "⚠️ Below minimum goal"
                        spent <= maxGoal -> "✅ Within budget goal!"
                        else -> "❌ Exceeded maximum goal"
                    }

                    binding.tvGoalStatus.setTextColor(
                        when {
                            spent < minGoal -> Color.parseColor("#FF9800")
                            spent <= maxGoal -> Color.parseColor("#4CAF50")
                            else -> Color.RED
                        }
                    )
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}