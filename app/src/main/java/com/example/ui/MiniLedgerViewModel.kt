package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MiniLedgerDatabase
import com.example.data.model.Loan
import com.example.data.model.LoanSummary
import com.example.data.model.UserSession
import com.example.data.repository.MiniLedgerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class Screen {
    object LoginPhone : Screen()
    object LoginOtp : Screen()
    object Dashboard : Screen()
    object AddLoan : Screen()
    object History : Screen()
    object Loans : Screen()
    object Settings : Screen()
    object Introduction : Screen()
}

data class MiniLedgerUiState(
    val currentScreen: Screen = Screen.LoginPhone,
    val phoneNumber: String = "",
    val countryCode: String = "+91",
    val formattedFullPhone: String = "",
    val otpDigits: List<String> = List(6) { "" },
    val latestGeneratedOtp: String? = null,
    val isSendingOtp: Boolean = false,
    val isVerifyingOtp: Boolean = false,
    val authError: String? = null,
    val countdownSeconds: Int = 30,
    val canResend: Boolean = false,
    val currentUser: UserSession? = null,
    val profilePhoto: String? = null,
    val isLoadingData: Boolean = false,
    // Add loan form state
    val newFriendName: String = "",
    val newAmount: String = "",
    val newNote: String = "",
    val newDueDateMillis: Long? = null,
    val addLoanError: String? = null,
    val isAddingLoan: Boolean = false,
    val feedbackMessage: String? = null
)

class MiniLedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val introPrefs = application.getSharedPreferences("miniledger_intro", android.content.Context.MODE_PRIVATE)
    private val repository: MiniLedgerRepository
    private val _uiState = MutableStateFlow(MiniLedgerUiState())
    val uiState: StateFlow<MiniLedgerUiState> = _uiState.asStateFlow()

    init {
        val db = MiniLedgerDatabase.getDatabase(application)
        repository = MiniLedgerRepository(application, db.loanDao())

        if(!introPrefs.getBoolean("seen", false)) _uiState.update { it.copy(currentScreen = Screen.Introduction) }
        // Restore login session if available
        val storedSession = repository.getStoredSession()
        if (storedSession != null) {
            _uiState.update {
                it.copy(
                    currentUser = storedSession,
                    profilePhoto = repository.getProfilePhoto(storedSession.phoneNumber),
                    formattedFullPhone = storedSession.phoneNumber,
                    currentScreen = if(introPrefs.getBoolean("seen", false)) Screen.Dashboard else Screen.Introduction
                )
            }
        }
    }

    private var countdownJob: Job? = null

    // Reactive streams for authenticated user (returns strictly real data, zero demo entries)
    val loans: StateFlow<List<Loan>> = _uiState
        .flatMapLatest { state ->
            val userId = state.currentUser?.phoneNumber
            if (userId != null) {
                repository.getLoansFlow(userId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val summary: StateFlow<LoanSummary> = _uiState
        .flatMapLatest { state ->
            val userId = state.currentUser?.phoneNumber
            if (userId != null) {
                repository.getSummaryFlow(userId)
            } else {
                flowOf(LoanSummary())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoanSummary()
        )

    fun onPhoneNumberChange(newPhone: String) {
        val digitsOnly = newPhone.filter { it.isDigit() || it.isWhitespace() }
        _uiState.update { it.copy(phoneNumber = digitsOnly, authError = null) }
    }

    fun requestOtp() {
        val raw = _uiState.value.phoneNumber.trim()
        if (raw.isEmpty()) {
            _uiState.update { it.copy(authError = "Please enter your phone number") }
            return
        }

        val fullPhone = "${_uiState.value.countryCode} $raw".trim()
        _uiState.update {
            it.copy(
                isSendingOtp = true,
                authError = null,
                formattedFullPhone = fullPhone
            )
        }

        viewModelScope.launch {
            val result = repository.sendOtp(fullPhone)
            if (result.isSuccess) {
                val generatedOtp = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        latestGeneratedOtp = generatedOtp,
                        otpDigits = List(6) { "" },
                        currentScreen = Screen.LoginOtp,
                        countdownSeconds = 30,
                        canResend = false,
                        feedbackMessage = "Mock OTP: $generatedOtp (Printed in server terminal)"
                    )
                }
                startCountdown()
            } else {
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        authError = result.exceptionOrNull()?.message ?: "Failed to send OTP"
                    )
                }
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 30 downTo 1) {
                _uiState.update { it.copy(countdownSeconds = i, canResend = false) }
                delay(1000)
            }
            _uiState.update { it.copy(countdownSeconds = 0, canResend = true) }
        }
    }

    fun onOtpDigitChange(index: Int, digit: String) {
        val currentDigits = _uiState.value.otpDigits.toMutableList()
        if (index in 0 until 6) {
            currentDigits[index] = digit.takeLast(1)
            _uiState.update { it.copy(otpDigits = currentDigits, authError = null) }
        }
    }

    fun autoFillMockOtp() {
        val otp = _uiState.value.latestGeneratedOtp ?: return
        val digits = otp.map { it.toString() }.take(6)
        _uiState.update { it.copy(otpDigits = digits, authError = null) }
    }

    fun verifyOtp() {
        val enteredOtp = _uiState.value.otpDigits.joinToString("")
        if (enteredOtp.length < 6) {
            _uiState.update { it.copy(authError = "Please enter the complete 6-digit code") }
            return
        }

        _uiState.update { it.copy(isVerifyingOtp = true, authError = null) }

        viewModelScope.launch {
            val phone = _uiState.value.formattedFullPhone
            val result = repository.verifyOtp(phone, enteredOtp)

            if (result.isSuccess) {
                val session = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isVerifyingOtp = false,
                        currentUser = session,
                        profilePhoto = session?.let { repository.getProfilePhoto(it.phoneNumber) },
                        currentScreen = Screen.Dashboard,
                        authError = null,
                        feedbackMessage = "Logged in successfully"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isVerifyingOtp = false,
                        authError = result.exceptionOrNull()?.message ?: "Invalid OTP code"
                    )
                }
            }
        }
    }

    fun resendOtp() {
        if (!_uiState.value.canResend) return
        val phone = _uiState.value.formattedFullPhone
        viewModelScope.launch {
            val result = repository.sendOtp(phone)
            if (result.isSuccess) {
                val newOtp = result.getOrNull()
                _uiState.update {
                    it.copy(
                        latestGeneratedOtp = newOtp,
                        countdownSeconds = 30,
                        canResend = false,
                        otpDigits = List(6) { "" },
                        feedbackMessage = "New OTP: $newOtp (Printed in server terminal)"
                    )
                }
                startCountdown()
            } else {
                _uiState.update {
                    it.copy(authError = result.exceptionOrNull()?.message ?: "Failed to resend OTP")
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen, authError = null, addLoanError = null) }
    }

    fun finishIntroduction() {
        introPrefs.edit().putBoolean("seen", true).apply()
        navigateTo(if(_uiState.value.currentUser != null) Screen.Dashboard else Screen.LoginPhone)
    }

    fun navigateBack() {
        if(_uiState.value.currentScreen == Screen.Introduction) { finishIntroduction(); return }

        _uiState.update { state ->
            val prev = when (state.currentScreen) {
                Screen.LoginOtp -> Screen.LoginPhone
                Screen.AddLoan -> Screen.Dashboard
                Screen.History -> Screen.Dashboard
                Screen.Loans -> Screen.Dashboard
                Screen.Settings -> Screen.Dashboard
                Screen.Introduction -> Screen.LoginPhone
                Screen.Dashboard -> Screen.Dashboard
                Screen.LoginPhone -> Screen.LoginPhone
            }
            state.copy(currentScreen = prev, authError = null, addLoanError = null)
        }
    }

    fun markLoanAsRepaid(loan: Loan) {
        viewModelScope.launch {
            val token = _uiState.value.currentUser?.token
            val result = repository.markLoanAsRepaid(loan.id, token)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(feedbackMessage = "Marked ${loan.friendName}'s loan as repaid!")
                }
            }
        }
    }

    fun onFriendNameChange(name: String) {
        _uiState.update { it.copy(newFriendName = name, addLoanError = null) }
    }

    fun onAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(newAmount = filtered, addLoanError = null) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(newNote = note.take(500), addLoanError = null) }
    }

    fun onDueDateChange(dateMillis: Long) {
        _uiState.update { it.copy(newDueDateMillis = dateMillis, addLoanError = null) }
    }

    fun submitNewLoan() {
        val state = _uiState.value

        // Prevent duplicate submissions
        if (state.isAddingLoan) return

        val name = state.newFriendName.trim()
        val amountNum = state.newAmount.toDoubleOrNull()
        val dueDate = state.newDueDateMillis

        if (name.isEmpty()) {
            _uiState.update { it.copy(addLoanError = "Friend's name is required") }
            return
        }

        if (amountNum == null || amountNum <= 0) {
            _uiState.update { it.copy(addLoanError = "Please enter an amount greater than 0") }
            return
        }

        if (dueDate == null || dueDate <= 0) {
            _uiState.update { it.copy(addLoanError = "Please select a valid due date") }
            return
        }

        val user = state.currentUser
        if (user == null) {
            _uiState.update { it.copy(addLoanError = "User session expired. Please log in again.") }
            return
        }

        _uiState.update { it.copy(isAddingLoan = true, addLoanError = null) }

        viewModelScope.launch {
            val result = repository.addLoan(
                userId = user.phoneNumber,
                friendName = name,
                amount = amountNum,
                dueDateMillis = dueDate,
                token = user.token,
                note = state.newNote
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isAddingLoan = false,
                        newFriendName = "",
                        newAmount = "",
                        newNote = "",
                        newDueDateMillis = null,
                        currentScreen = Screen.Dashboard,
                        feedbackMessage = "Loan to $name saved successfully!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isAddingLoan = false,
                        addLoanError = result.exceptionOrNull()?.message ?: "Failed to save loan"
                    )
                }
            }
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    fun updateProfile(name: String) {
        val user = _uiState.value.currentUser ?: return
        val updated = user.copy(name = name.trim().take(50))
        repository.saveSession(updated)
        _uiState.update { it.copy(currentUser = updated) }
    }

    fun updateProfilePhoto(uri: String?) {
        val user = _uiState.value.currentUser ?: return
        repository.setProfilePhoto(user.phoneNumber, uri)
        _uiState.update { it.copy(profilePhoto = uri) }
    }

    fun logout() {
        repository.clearSession()
        _uiState.update {
            it.copy(
                currentUser = null,
                profilePhoto = null,
                phoneNumber = "",
                formattedFullPhone = "",
                currentScreen = Screen.LoginPhone,
                otpDigits = List(6) { "" },
                latestGeneratedOtp = null,
                authError = null
            )
        }
    }
}
