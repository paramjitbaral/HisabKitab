package com.example

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.model.*
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ReferenceDesignTest {
    @get:Rule val compose = createComposeRule()
    private val today=System.currentTimeMillis()
    private val loans=listOf(
        Loan(1,"test","Rahul",2000.0,today-86400000*10),
        Loan(2,"test","Aman",3000.0,today+86400000*5),
        Loan(3,"test","Siddharth",3000.0,today-86400000*15,true,today-86400000*3,note="Dinner"),
        Loan(4,"test","Neha",4500.0,today+86400000*15))
    @Test fun csvEscapesUserContentAndPreservesAmounts() {
        val csv=loanCsv(listOf(loans[0].copy(friendName="=SUM(1,2)",note="A \"shared\" meal\nPaid later")))
        assertTrue(csv.contains("\"'=SUM(1,2)\""))
        assertTrue(csv.contains("\"2000.00\""))
        assertTrue(csv.contains("\"A \"\"shared\"\" meal\nPaid later\""))
        assertTrue(loanCsv(emptyList()).startsWith("Friend,Amount (INR)"))
    }
    private fun screenshot(name:String) {
        compose.waitForIdle()
        android.os.SystemClock.sleep(1500) // Allow the window compositor to present the frame.
        val file=File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null),"$name.png")
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    @Test fun migrationPreservesExistingLoansAndAddsNotes() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="migration-reference-test.db"
        context.deleteDatabase(name)
        val file=context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(file,null).use { db ->
            db.execSQL("CREATE TABLE loans (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId TEXT NOT NULL, friendName TEXT NOT NULL, amount REAL NOT NULL, dueDateMillis INTEGER NOT NULL, isRepaid INTEGER NOT NULL, repaidDateMillis INTEGER, createdAtMillis INTEGER NOT NULL)")
            db.execSQL("INSERT INTO loans VALUES (1, 'test', 'Preserved friend', 500, 1000, 0, NULL, 1000)")
            db.version=1
        }
        val db=androidx.room.Room.databaseBuilder(context,com.example.data.local.MiniLedgerDatabase::class.java,name)
            .addMigrations(com.example.data.local.MiniLedgerDatabase.MIGRATION_1_2).build()
        try {
            db.openHelper.writableDatabase.query("SELECT friendName,note FROM loans WHERE id=1").use { cursor ->
                assertTrue(cursor.moveToFirst());assertEquals("Preserved friend",cursor.getString(0));assertEquals("",cursor.getString(1))
            }
        } finally { db.close();context.deleteDatabase(name) }
    }
    @Test fun homeUsesRealTotalsAndNavigatesToHistory() {
        var destination:Screen?=null
        compose.setContent { MyApplicationTheme { DashboardScreen(MiniLedgerUiState(),loans,LoanSummary(12500.0,3000.0,2000.0),{destination=it},{},{},{}) } }
        compose.onNodeWithText("₹ 12,500").assertExists()
        screenshot("reference-home")
        compose.onNodeWithText("View all").performClick()
        assertEquals(Screen.History,destination)
    }
    @Test fun formQuickAmountAndNoteReachSubmit() {
        var state=MiniLedgerUiState()
        var submitted=false
        compose.setContent {
            var current by remember { mutableStateOf(state) }
            MyApplicationTheme {
                AddLoanScreen(current,{current=current.copy(newFriendName=it);state=current},
                    {current=current.copy(newAmount=it);state=current},{current=current.copy(newDueDateMillis=it);state=current},
                    {submitted=true},{},onNoteChange={current=current.copy(newNote=it);state=current})
            }
        }
        screenshot("reference-add-loan")
        compose.onNodeWithTag("friend_name_input").performScrollTo().performTextInput("Test Friend")
        compose.onNodeWithText("₹2000").performScrollTo().performClick()
        compose.onNodeWithTag("note_input").performScrollTo().performTextInput("Shared trip")
        compose.onNodeWithTag("save_loan_button").performScrollTo().performClick()
        assertTrue(submitted)
        assertEquals("2000",state.newAmount)
        assertEquals("Shared trip",state.newNote)
    }
    @Test fun loansShowsOnlyOutstandingAndFiltersAttention() {
        compose.setContent { MyApplicationTheme { LoansScreen(loans,{},{}) } }
        compose.onNodeWithText("₹ 9,500").assertExists()
        compose.onNodeWithText("Siddharth").assertDoesNotExist()
        screenshot("reference-loans")
        compose.onNodeWithText("Needs attention").performClick()
        compose.onNodeWithText("Rahul").assertExists()
        compose.onNodeWithText("Aman").assertDoesNotExist()
    }
    @Test fun historyFiltersExcludeOtherStatuses() {
        compose.setContent { MyApplicationTheme { HistoryScreen(loans,{},{}) } }
        screenshot("reference-history")
        compose.onAllNodesWithText("Repaid")[0].performClick()
        compose.onNodeWithText("Siddharth").assertExists()
        compose.onNodeWithText("Rahul").assertDoesNotExist()
        compose.onNodeWithText("Aman").assertDoesNotExist()
        compose.onNodeWithContentDescription("Search loans").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("missing")
        compose.onNodeWithText("No matching loans.").assertExists()
    }
    @Test fun settingsEditsProfileAndHomeAvatarOpensSettings() {
        var destination: Screen? = null
        var savedName = ""
        var showSettings by mutableStateOf(false)
        compose.setContent { MyApplicationTheme {
            if(showSettings) SettingsScreen(MiniLedgerUiState(), {}, {savedName=it}, {}, {})
            else DashboardScreen(MiniLedgerUiState(), emptyList(), LoanSummary(), {destination=it;showSettings=true}, {}, {}, {})
        } }
        compose.onNodeWithTag("profile_avatar_button").performClick()
        assertEquals(Screen.Settings, destination)
        compose.onNodeWithTag("settings_screen").assertExists()
        screenshot("reference-settings")
        compose.onNodeWithText("Edit Profile").performClick()
        compose.onNodeWithTag("profile_name_input").performScrollTo().performTextInput("Priya")
        compose.onNodeWithText("Save profile").performScrollTo().performClick()
        assertEquals("Priya", savedName)
        compose.onNodeWithText("Saved").assertExists()
    }
    @Test fun introductionAdvancesThroughThreePagesAndFinishes() {
        var finished = false
        compose.setContent { MyApplicationTheme { IntroductionScreen { finished = true } } }
        screenshot("intro-page-1")
        compose.onNodeWithTag("intro_next").performClick()
        compose.waitForIdle()
        screenshot("intro-page-2")
        compose.onNodeWithTag("intro_next").performClick()
        compose.onNodeWithText("Get Started").assertExists()
        screenshot("intro-page-3")
        compose.onNodeWithTag("intro_next").performClick()
        assertTrue(finished)
    }
    @Test fun loanSheetDismissesWithoutRepaymentAndSubmitsOnce() {
        var paid=0
        var open by mutableStateOf(true)
        compose.setContent { MyApplicationTheme { if(open) com.example.ui.components.LoanDetailsDialog(loans[0],{open=false},{paid++;open=false}) } }
        compose.onNodeWithText("Loan details").assertExists()
        screenshot("reference-loan-sheet")
        compose.onNodeWithContentDescription("Close loan details").performClick()
        assertEquals(0,paid)
        compose.runOnIdle { open=true }
        compose.onNodeWithText("Mark as repaid").performClick()
        compose.runOnIdle { assertEquals(1,paid) }
    }
    @Test fun settingsSectionsOpenPagesAndReturnToProfile() {
        compose.setContent { MyApplicationTheme { SettingsScreen(MiniLedgerUiState(),{},{},{},{}) } }
        compose.onNodeWithText("Your Data").performScrollTo().performClick()
        compose.onNodeWithTag("settings_detail_page").assertExists()
        compose.onNodeWithText("Export loans as CSV").assertExists()
        screenshot("reference-settings-detail")
        compose.onNodeWithContentDescription("Back to profile").performClick()
        compose.onNodeWithTag("settings_screen").assertExists()
        compose.onNodeWithText("Help & Support").performScrollTo().performClick()
        compose.onNodeWithText("Record a repayment").assertExists()
    }
}

