package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MiniLedgerViewModel
import com.example.ui.Screen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.AddLoanScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OtpVerifyScreen
import com.example.ui.screens.LoginPhoneScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WarmBackground
                ) {
                    MiniLedgerApp()
                }
            }
        }
    }
}

@Composable
fun MiniLedgerApp(
    viewModel: MiniLedgerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.SideEffect {
        val window = (view.context as android.app.Activity).window
        androidx.core.view.WindowCompat.getInsetsController(window, view)
            .isAppearanceLightStatusBars = uiState.currentScreen != Screen.LoginPhone && uiState.currentScreen != Screen.Dashboard && uiState.currentScreen != Screen.AddLoan && uiState.currentScreen != Screen.History && uiState.currentScreen != Screen.Loans && uiState.currentScreen != Screen.Settings
    }



    // Handle back press
    BackHandler(enabled = uiState.currentScreen != Screen.LoginPhone && uiState.currentScreen != Screen.Dashboard) {
        viewModel.navigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                Screen.LoginPhone -> {
                    LoginPhoneScreen(
                        uiState = uiState,
                        onPhoneNumberChange = viewModel::onPhoneNumberChange,
                        onRequestOtp = viewModel::requestOtp
                    )
                }

                Screen.LoginOtp -> {
                    com.example.ui.screens.OtpVerifyScreen(
                        uiState = uiState,
                        onDigitChange = viewModel::onOtpDigitChange,
                        onAutoFill = viewModel::autoFillMockOtp,
                        onVerify = viewModel::verifyOtp,
                        onResend = viewModel::resendOtp,
                        onBack = viewModel::navigateBack
                    )
                }

                Screen.Dashboard -> {
                    DashboardScreen(
                        uiState = uiState,
                        loans = loans,
                        summary = summary,
                        onNavigate = viewModel::navigateTo,
                        onMarkAsRepaid = viewModel::markLoanAsRepaid,
                        onLogout = viewModel::logout,
                        onClearFeedback = viewModel::clearFeedbackMessage
                    )
                }

                Screen.Introduction -> {
                    com.example.ui.screens.IntroductionScreen(viewModel::finishIntroduction)
                }
                Screen.Settings -> {
                    com.example.ui.screens.SettingsScreen(uiState, viewModel::navigateBack,
                        viewModel::updateProfile, viewModel::updateProfilePhoto, viewModel::logout, { viewModel.navigateTo(Screen.Introduction) }, viewModel::navigateTo)
                }
                Screen.Loans -> {
                    com.example.ui.screens.LoansScreen(loans=loans, onNavigate=viewModel::navigateTo,
                        onMarkAsRepaid=viewModel::markLoanAsRepaid)
                }
                Screen.History -> {
                    HistoryScreen(loans = loans, onNavigate = viewModel::navigateTo,
                        onMarkAsRepaid = viewModel::markLoanAsRepaid)
                }
                Screen.AddLoan -> {
                    AddLoanScreen(
                        uiState = uiState,
                        onFriendNameChange = viewModel::onFriendNameChange,
                        onAmountChange = viewModel::onAmountChange,
                        onNoteChange = viewModel::onNoteChange,
                        onNavigate = viewModel::navigateTo,
                        onDueDateChange = viewModel::onDueDateChange,
                        onSubmit = viewModel::submitNewLoan,
                        onBack = viewModel::navigateBack
                    )
                }
            }
        }
    }
}
