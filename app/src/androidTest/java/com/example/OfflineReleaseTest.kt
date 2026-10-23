package com.example

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.local.MiniLedgerDatabase
import com.example.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OfflineReleaseTest {
    private val loan=Loan(userId="old-profile",friendName="A friend",amount=1234.56,dueDateMillis=1800000000000,createdAtMillis=1700000000000,note="Trip, dinner\nShared")
    @Test fun backupRoundTripExcludesIdentityAndRejectsInvalidData() {
        val encoded=LoanBackup.encode(listOf(loan))
        assertFalse(encoded.contains("old-profile"))
        assertEquals(loan.copy(userId=""),LoanBackup.decode(encoded).single())
        assertTrue(runCatching { LoanBackup.decode(encoded.replace("1234.56","-1")) }.isFailure)
        assertTrue(runCatching { LoanBackup.decode(encoded.replace("\"version\": 1","\"version\": 99")) }.isFailure)
    }
    @Test fun restoreIsAtomicAndSkipsExactDuplicatesAndDeletionClearsData() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,MiniLedgerDatabase::class.java).build()
        try {
            val dao=db.loanDao()
            assertEquals(1,dao.mergeBackup("local-ledger",listOf(loan)))
            assertEquals(0,dao.mergeBackup("local-ledger",listOf(loan)))
            val restored=dao.getLoansSnapshot("local-ledger").single()
            assertEquals("Trip, dinner\nShared",restored.note)
            dao.markAsRepaid(restored.id,1800000000001)
            assertTrue(dao.getLoansSnapshot("local-ledger").single().isRepaid)
            dao.deleteAll()
            assertTrue(dao.getLoansSnapshot("local-ledger").isEmpty())
        } finally { db.close() }
    }
}
