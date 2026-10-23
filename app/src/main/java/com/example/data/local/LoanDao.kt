package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun getLoansForUser(userId: String): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE userId = :userId")
    suspend fun getLoansSnapshot(userId: String): List<Loan>

    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    suspend fun getLoanById(loanId: Long): Loan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLoan(loan: Loan)

    @Query("UPDATE loans SET isRepaid = 1, repaidDateMillis = :repaidDateMillis WHERE id = :loanId")
    suspend fun markAsRepaid(loanId: Long, repaidDateMillis: Long)

    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoan(loanId: Long)

    @Query("SELECT COUNT(*) FROM loans WHERE userId = :userId")
    suspend fun getLoanCount(userId: String): Int
}
