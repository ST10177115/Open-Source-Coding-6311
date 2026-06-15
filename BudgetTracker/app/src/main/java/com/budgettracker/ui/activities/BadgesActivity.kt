package com.budgettracker.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityBadgesBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager

data class Badge(
    val title: String,
    val description: String,
    val emoji: String,
    val isEarned: Boolean
)

class BadgesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBadgesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBadgesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Badges"

        loadBadges()
    }

    private fun loadBadges() {
        val userId = sessionManager.getUserId()
        val startDate = DateUtils.getFirstDayOfMonthDbString()
        val endDate = DateUtils.getTodayDbString()

        db.expenseDao().getCategoryTotalsForPeriod(userId, startDate, endDate)
            .observe(this) { totals ->
                db.expenseDao().getTotalSpentForPeriod(userId, startDate, endDate)
                    .observe(this) { total ->
                        val spent = total ?: 0.0

                        db.userDao().getUserById(userId).observe(this) { user ->
                            val minGoal = user?.minMonthlyGoal ?: 0.0
                            val maxGoal = user?.maxMonthlyGoal ?: 0.0

                            val badges = listOf(
                                Badge(
                                    "First Expense",
                                    "Logged your first expense",
                                    "🎯",
                                    totals.isNotEmpty()
                                ),
                                Badge(
                                    "Budget Master",
                                    "Stayed within your maximum goal",
                                    "🏆",
                                    maxGoal > 0 && spent <= maxGoal && spent > 0
                                ),
                                Badge(
                                    "Goal Setter",
                                    "Set your monthly min and max goals",
                                    "🎪",
                                    minGoal > 0 && maxGoal > 0
                                ),
                                Badge(
                                    "Category Pro",
                                    "Logged expenses in 3 or more categories",
                                    "📊",
                                    totals.size >= 3
                                ),
                                Badge(
                                    "Super Saver",
                                    "Spent less than your minimum goal",
                                    "💰",
                                    minGoal > 0 && spent < minGoal && spent > 0
                                ),
                                Badge(
                                    "Tracker",
                                    "Logged expenses in 5 or more categories",
                                    "⭐",
                                    totals.size >= 5
                                )
                            )

                            displayBadges(badges)
                        }
                    }
            }
    }

    private fun displayBadges(badges: List<Badge>) {
        binding.badgesContainer.removeAllViews()

        val earnedCount = badges.count { it.isEarned }
        binding.tvEarnedCount.text = "Badges Earned: $earnedCount / ${badges.size}"

        badges.forEach { badge ->
            val card = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(0, 0, 0, 16) }
                radius = 12f
                cardElevation = 4f
                setCardBackgroundColor(
                    if (badge.isEarned) Color.parseColor("#E8F5E9")
                    else Color.parseColor("#F5F5F5")
                )
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24, 24, 24, 24)
            }

            val emoji = TextView(this).apply {
                text = if (badge.isEarned) badge.emoji else "🔒"
                textSize = 32f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(0, 0, 24, 0) }
            }

            val textLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val title = TextView(this).apply {
                text = badge.title
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(
                    if (badge.isEarned) Color.parseColor("#2E7D32")
                    else Color.parseColor("#9E9E9E")
                )
            }

            val desc = TextView(this).apply {
                text = badge.description
                textSize = 13f
                setTextColor(
                    if (badge.isEarned) Color.parseColor("#555555")
                    else Color.parseColor("#9E9E9E")
                )
            }

            textLayout.addView(title)
            textLayout.addView(desc)
            row.addView(emoji)
            row.addView(textLayout)
            card.addView(row)
            binding.badgesContainer.addView(card)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
