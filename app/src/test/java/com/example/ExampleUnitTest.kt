package com.example

import com.example.data.model.Loan
import com.example.data.model.LoanSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MiniLedgerLogicTest {

    @Test
    fun testLoanOverdueLogic() {
        val now = System.currentTimeMillis()
        val yesterday = now - (24 * 60 * 60 * 1000L)
        val tomorrow = now + (24 * 60 * 60 * 1000L)

        // 1. Unpaid past due loan should be overdue
        val overdueLoan = Loan(
            userId = "+91 98765 43210",
            friendName = "Rahul",
            amount = 2000.0,
            dueDateMillis = yesterday - (5 * 24 * 60 * 60 * 1000L),
            isRepaid = false
        )
        assertTrue(overdueLoan.isOverdue(now))

        // 2. Repaid past due loan should NOT be overdue
        val repaidLoan = Loan(
            userId = "+91 98765 43210",
            friendName = "Siddharth",
            amount = 3000.0,
            dueDateMillis = yesterday,
            isRepaid = true
        )
        assertFalse(repaidLoan.isOverdue(now))

        // 3. Unpaid future loan should NOT be overdue
        val pendingLoan = Loan(
            userId = "+91 98765 43210",
            friendName = "Aman",
            amount = 3000.0,
            dueDateMillis = tomorrow,
            isRepaid = false
        )
        assertFalse(pendingLoan.isOverdue(now))

        // 4. An unpaid loan due today is NOT overdue until the date has passed
        val dueTodayLoan = Loan(
            userId = "+91 98765 43210",
            friendName = "Karan",
            amount = 1000.0,
            dueDateMillis = now,
            isRepaid = false
        )
        assertFalse(dueTodayLoan.isOverdue(now))
    }

    @Test
    fun testSummaryCalculations() {
        val now = System.currentTimeMillis()
        val pastDate = now - (10 * 24 * 60 * 60 * 1000L)
        val futureDate = now + (10 * 24 * 60 * 60 * 1000L)

        val loans = listOf(
            Loan(id = 1, userId = "user1", friendName = "Rahul", amount = 2000.0, dueDateMillis = pastDate, isRepaid = false),
            Loan(id = 2, userId = "user1", friendName = "Aman", amount = 3000.0, dueDateMillis = futureDate, isRepaid = false),
            Loan(id = 3, userId = "user1", friendName = "Siddharth", amount = 4000.0, dueDateMillis = pastDate, isRepaid = true),
            Loan(id = 4, userId = "user1", friendName = "Neha", amount = 3500.0, dueDateMillis = futureDate, isRepaid = false)
        )

        var totalLent = 0.0
        var totalRepaid = 0.0
        var overdueAmount = 0.0

        for (loan in loans) {
            totalLent += loan.amount
            if (loan.isRepaid) {
                totalRepaid += loan.amount
            } else if (loan.isOverdue(now)) {
                overdueAmount += loan.amount
            }
        }

        // Total lent: 2000 + 3000 + 4000 + 3500 = 12500
        assertEquals(12500.0, totalLent, 0.001)
        // Total repaid: 4000
        assertEquals(4000.0, totalRepaid, 0.001)
        // Overdue amount: Rahul (2000)
        assertEquals(2000.0, overdueAmount, 0.001)
    }
}
