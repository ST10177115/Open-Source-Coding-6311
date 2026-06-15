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
import com.budgettracker.databinding.ActivityBudgetTipsBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager

class BudgetTipsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBudgetTipsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Budget Tips"
        loadTips()
    }

    private fun loadTips() {
        val userId = sessionManager.getUserId()
        val startDate = DateUtils.getFirstDayOfMonthDbString()
        val endDate = DateUtils.getTodayDbString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
