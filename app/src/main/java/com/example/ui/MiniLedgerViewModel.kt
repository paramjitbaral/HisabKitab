package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MiniLedgerDatabase
import com.example.data.model.Loan
import com.example.data.model.LoanSummary
import com.example.data.model.UserSession
import com.example.data.repository.MiniLedgerRepository
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
    object Dashboard : Screen()
    object AddLoan : Screen()
    object History : Screen()
    object Loans : Screen()
    object Settings : Screen()
    object Introduction : Screen()
}

data class MiniLedgerUiState(
    val currentScreen: Screen = Screen.Introduction,
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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MiniLedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val introPrefs = application.getSharedPreferences("miniledger_intro", android.content.Context.MODE_PRIVATE)
    private val repository: MiniLedgerRepository
    private val _uiState = MutableStateFlow(MiniLedgerUiState())
    val uiState: StateFlow<MiniLedgerUiState> = _uiState.asStateFlow()

    init {
        val db = MiniLedgerDatabase.getDatabase(application)
        repository = MiniLedgerRepository(application, db.loanDao())

        if(!introPrefs.getBoolean("seen", false)) _uiState.update { it.copy(currentScreen = Screen.Introduction) }
        val profile=repository.ensureLocalProfile()
        _uiState.update { it.copy(currentUser=profile,profilePhoto=repository.getProfilePhoto(profile.phoneNumber),currentScreen=if(introPrefs.getBoolean("seen",false)) Screen.Dashboard else Screen.Introduction) }
    }


    // Observe records belonging to the local profile.
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

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen, addLoanError = null) }
    }

    fun finishIntroduction() {
        introPrefs.edit().putBoolean("seen", true).apply()
        navigateTo(Screen.Dashboard)
    }

    fun navigateBack() {
        if(_uiState.value.currentScreen == Screen.Introduction) { finishIntroduction(); return }

        _uiState.update { state ->
            val prev = when (state.currentScreen) {
                Screen.AddLoan -> Screen.Dashboard
                Screen.History -> Screen.Dashboard
                Screen.Loans -> Screen.Dashboard
                Screen.Settings -> Screen.Dashboard
                Screen.Introduction -> Screen.Dashboard
                Screen.Dashboard -> Screen.Dashboard
            }
            state.copy(currentScreen = prev, addLoanError = null)
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
        _uiState.update { it.copy(newFriendName = name.take(100), addLoanError = null) }
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

        if (amountNum == null || !amountNum.isFinite() || amountNum <= 0 || amountNum > 999999999.99) {
            _uiState.update { it.copy(addLoanError = "Please enter an amount greater than 0") }
            return
        }

        if (dueDate == null || dueDate <= 0) {
            _uiState.update { it.copy(addLoanError = "Please select a valid due date") }
            return
        }

        val user = state.currentUser
        if (user == null) {
            _uiState.update { it.copy(addLoanError = "Could not open your local profile. Please restart the app.") }
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

    suspend fun restoreBackup(loans:List<Loan>):Int = repository.restore(requireNotNull(_uiState.value.currentUser).phoneNumber,loans)
    suspend fun eraseAllData() {
        repository.eraseAll()
        val profile=repository.ensureLocalProfile()
        _uiState.update { MiniLedgerUiState(currentScreen=Screen.Settings,currentUser=profile) }
    }
    fun logout() { navigateTo(Screen.Settings) } // Legacy callback; offline UI has no sign-out action.
}
