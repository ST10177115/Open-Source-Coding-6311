package com.budgettracker.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityMainBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Budget Tracker"

        binding.tvWelcome.text = "Welcome, ${sessionManager.getUsername()}!"

        observeMonthlyTotal()
        setupClickListeners()
    }

    private fun observeMonthlyTotal() {
        val userId = sessionManager.getUserId()
        val startDate = DateUtils.getFirstDayOfMonthDbString()
        val endDate = DateUtils.getTodayDbString()

        db.expenseDao().getTotalSpentForPeriod(userId, startDate, endDate)
            .observe(this) { total ->
                val amount = total ?: 0.0
                binding.tvMonthlyTotal.text = DateUtils.formatCurrency(amount)

                lifecycleScope.launch {
                    val user = db.userDao().getUserById(userId)
                    user.observe(this@MainActivity) { u ->
                        if (u != null && (u.minMonthlyGoal > 0 || u.maxMonthlyGoal > 0)) {
                            val min = u.minMonthlyGoal
                            val max = u.maxMonthlyGoal
                            binding.tvGoalStatus.text = when {
                                max > 0 && amount > max ->
                                    "⚠ Over budget! (max: ${DateUtils.formatCurrency(max)})"
                                min > 0 && amount < min ->
                                    "📉 Below min goal (${DateUtils.formatCurrency(min)})"
                                else ->
                                    "✓ On track (${DateUtils.formatCurrency(min)} – ${DateUtils.formatCurrency(max)})"
                            }
                        } else {
                            binding.tvGoalStatus.text = "No goals set — tap Goals to configure"
                        }
                    }
                }
            }
    }

    private fun setupClickListeners() {
        binding.cardAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.cardCategories.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
        }

        binding.cardViewExpenses.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }

        binding.cardCategoryReport.setOnClickListener {
            startActivity(Intent(this, CategoryReportActivity::class.java))
        }

        binding.cardGoals.setOnClickListener {
            startActivity(Intent(this, GoalSettingsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.cardGraph.setOnClickListener {
            startActivity(Intent(this, GraphActivity::class.java))
        }

        binding.cardBadges.setOnClickListener {
            startActivity(Intent(this, BadgesActivity::class.java))
        }
        binding.cardMonthlySummary.setOnClickListener {
            startActivity(Intent(this, MonthlySummaryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        observeMonthlyTotal()
    }
}
