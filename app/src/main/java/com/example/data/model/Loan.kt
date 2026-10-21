package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String, // Authenticated user phone / id
    val friendName: String,
    val amount: Double,
    val dueDateMillis: Long,
    val isRepaid: Boolean = false,
    val repaidDateMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    @androidx.room.ColumnInfo(defaultValue = "''")
    val note: String = ""
) {
    /**
     * An entry is overdue only when it is unpaid and its due date is strictly before today's date.
     * An unpaid loan due today is not overdue until the date has passed.
     */
    fun isOverdue(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        if (isRepaid) return false

        // Get start of today (midnight 00:00:00.000)
        val todayStart = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Get start of due date day (midnight 00:00:00.000)
        val dueDayStart = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Strictly before today's date
        return dueDayStart.timeInMillis < todayStart.timeInMillis
    }

    fun formattedDueDate(): String {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        return sdf.format(Date(dueDateMillis))
    }

    fun formattedRepaidDate(): String {
        val dateToFormat = repaidDateMillis ?: System.currentTimeMillis()
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        return sdf.format(Date(dateToFormat))
    }
}

data class LoanSummary(
    val totalLent: Double = 0.0,
    val totalRepaid: Double = 0.0,
    val overdueAmount: Double = 0.0
)
