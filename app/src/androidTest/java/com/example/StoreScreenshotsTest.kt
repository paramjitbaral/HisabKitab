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
import java.io.File

/** Screenshots use fictional sample loans, never the device owner's records. */
class StoreScreenshotsTest {
    @get:Rule val compose=createComposeRule()
    @Test fun captureListingScreens() {
        val now=System.currentTimeMillis()
        val loans=listOf(Loan(1,"sample","Rahul",2000.0,now-86400000*4,note="Shared trip"),Loan(2,"sample","Aman",3000.0,now+86400000*5,note="Course books"),Loan(3,"sample","Neha",4500.0,now-86400000*2,true,now-86400000,note="Dinner"))
        var page by mutableStateOf(0)
        compose.setContent { MyApplicationTheme { when(page) {
            0 -> DashboardScreen(MiniLedgerUiState(currentUser=UserSession("sample","Alex","")),loans,LoanSummary(9500.0,4500.0,2000.0),{},{},{},{})
            1 -> HistoryScreen(loans,{},{})
            2 -> AddLoanScreen(MiniLedgerUiState(),{},{},{},{},{})
            else -> SettingsScreen(MiniLedgerUiState(currentUser=UserSession("sample","Alex","")),{},{},{},{},loans=loans)
        } } }
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        listOf("home","history","add-loan","profile").forEachIndexed { index,name ->
            compose.runOnIdle { page=index };compose.waitForIdle()
            compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
                File(context.getExternalFilesDir(null),"store-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            }
        }
    }
}
