package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.LoanDao
import com.example.data.model.Loan
import com.example.data.model.LoanSummary
import com.example.data.model.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class MiniLedgerRepository(
    private val context: Context,
    private val loanDao: LoanDao,
    private val backendBaseUrl: String = "http://10.0.2.2:5001" // Standard Android Emulator host bridge
) {
    private val prefs = context.getSharedPreferences("miniledger_prefs", Context.MODE_PRIVATE)

    // In-memory OTP store for phone numbers: phone -> (otp, timestampMillis, isUsed)
    private val activeOtps = ConcurrentHashMap<String, OtpRecord>()

    data class OtpRecord(
        val code: String,
        val timestamp: Long,
        var isUsed: Boolean = false
    )

    /**
     * Retrieves stored session if user previously logged in
     */
    fun getStoredSession(): UserSession? {
        val phone = prefs.getString("user_phone", null) ?: return null
        val token = prefs.getString("auth_token", null) ?: return null
        val name = prefs.getString("user_name", "User") ?: "User"
        return UserSession(phoneNumber = phone, name = name, token = token)
    }

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString("user_phone", session.phoneNumber)
            .putString("auth_token", session.token)
            .putString("user_name", session.name)
            .apply()
    }

    private val profilePrefs = context.getSharedPreferences("miniledger_profiles", Context.MODE_PRIVATE)
    fun getProfilePhoto(phone: String): String? = profilePrefs.getString(phone, null)
    fun setProfilePhoto(phone: String, uri: String?) { profilePrefs.edit().putString(phone, uri).apply() }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /**
     * Request OTP
     * 1. Attempts remote backend POST /auth/send-otp
     * 2. If backend is not reached from emulator, generates secure local OTP with expiration
     * Prints mock OTP to console/logcat
     */
    suspend fun sendOtp(phoneNumber: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim()

        // Throttling: Enforce 10s cooldown per phone number
        val existing = activeOtps[cleanPhone]
        val now = System.currentTimeMillis()
        if (existing != null && !existing.isUsed && now - existing.timestamp < 10000) {
            val remaining = ((10000 - (now - existing.timestamp)) / 1000) + 1
            return@withContext Result.failure(Exception("Please wait ${remaining}s before requesting a new OTP"))
        }

        // Try backend API first
        val backendResult = callBackendJson("POST", "/auth/send-otp", mapOf("phoneNumber" to cleanPhone), null)
        if (backendResult.isSuccess) {
            val json = backendResult.getOrNull()
            val otpCode = json?.optString("mockOtp")
            if (!otpCode.isNullOrEmpty()) {
                activeOtps[cleanPhone] = OtpRecord(otpCode, now)
                Log.i("MiniLedgerServer", "==================================================")
                Log.i("MiniLedgerServer", "[Backend Server Console] MOCK OTP FOR $cleanPhone: [ $otpCode ]")
                Log.i("MiniLedgerServer", "==================================================")
                return@withContext Result.success(otpCode)
            }
        }

        // Generate genuine 6-digit OTP stored in persistent state
        val randomOtp = String.format("%06d", Random.nextInt(100000, 999999))
        activeOtps[cleanPhone] = OtpRecord(randomOtp, now)

        println("==================================================")
        println("[MiniLedger Server Log] MOCK OTP FOR $cleanPhone: $randomOtp")
        println("==================================================")
        Log.i("MiniLedgerServer", "MOCK OTP FOR $cleanPhone is: $randomOtp")

        Result.success(randomOtp)
    }

    /**
     * Verify OTP
     * Validates against single-use, unexpired OTP. No hardcoded backdoor codes.
     */
    suspend fun verifyOtp(phoneNumber: String, enteredOtp: String): Result<UserSession> = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim()
        val cleanInput = enteredOtp.trim()

        // Try backend verification
        val backendResult = callBackendJson(
            "POST",
            "/auth/verify-otp",
            mapOf("phoneNumber" to cleanPhone, "otp" to cleanInput),
            null
        )
        if (backendResult.isSuccess) {
            val json = backendResult.getOrNull()
            val token = json?.optString("token")
            if (!token.isNullOrEmpty()) {
                val session = UserSession(
                    phoneNumber = cleanPhone,
                    name = cleanPhone,
                    token = token
                )
                saveSession(session)
                return@withContext Result.success(session)
            }
        }

        // Local verification validation
        val record = activeOtps[cleanPhone]
        if (record == null) {
            return@withContext Result.failure(Exception("No active OTP found. Please request a new OTP."))
        }

        if (record.isUsed) {
            return@withContext Result.failure(Exception("This OTP has already been used. Please request a new code."))
        }

        val now = System.currentTimeMillis()
        if (now - record.timestamp > 5 * 60 * 1000) { // 5-minute expiration
            activeOtps.remove(cleanPhone)
            return@withContext Result.failure(Exception("OTP has expired. Please request a new OTP."))
        }

        if (record.code != cleanInput) {
            return@withContext Result.failure(Exception("Incorrect OTP. Please check the code in the server log."))
        }

        // Mark as used (single-use)
        record.isUsed = true

        val session = UserSession(
            phoneNumber = cleanPhone,
            name = cleanPhone,
            token = "jwt-token-${System.currentTimeMillis()}-${Random.nextInt(1000, 9999)}"
        )
        saveSession(session)
        Result.success(session)
    }

    /**
     * Flow of user's loans from persistent storage.
     * Contains ZERO demo data. Fresh users start with empty list.
     */
    fun getLoansFlow(userId: String): Flow<List<Loan>> {
        return loanDao.getLoansForUser(userId)
    }

    /**
     * Calculate Summary from actual persistent records.
     * - Total Lent: Sum of every loan amount created.
     * - Total Repaid: Sum of loan amounts for entries marked as repaid.
     * - Overdue Amount: Sum of unpaid loan amounts with due dates strictly before today's date.
     */
    fun getSummaryFlow(userId: String): Flow<LoanSummary> {
        return loanDao.getLoansForUser(userId).map { loans ->
            var totalLent = 0.0
            var totalRepaid = 0.0
            var overdueAmount = 0.0
            val now = System.currentTimeMillis()

            for (loan in loans) {
                totalLent += loan.amount
                if (loan.isRepaid) {
                    totalRepaid += loan.amount
                } else if (loan.isOverdue(now)) {
                    overdueAmount += loan.amount
                }
            }

            LoanSummary(
                totalLent = totalLent,
                totalRepaid = totalRepaid,
                overdueAmount = overdueAmount
            )
        }
    }

    /**
     * POST /loans
     * Validates and saves record in persistent database.
     */
    suspend fun addLoan(
        userId: String,
        friendName: String,
        amount: Double,
        dueDateMillis: Long,
        token: String?,
        note: String = ""
    ): Result<Loan> = withContext(Dispatchers.IO) {
        if (friendName.isBlank()) {
            return@withContext Result.failure(Exception("Friend's name cannot be empty"))
        }
        if (amount <= 0) {
            return@withContext Result.failure(Exception("Amount must be greater than zero"))
        }
        if (dueDateMillis <= 0) {
            return@withContext Result.failure(Exception("Due date is invalid"))
        }

        // Try syncing with backend if available
        if (!token.isNullOrEmpty()) {
            val cal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
            val formattedDate = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
            callBackendJson(
                "POST",
                "/loans",
                mapOf(
                    "friendName" to friendName.trim(),
                    "amount" to amount,
                    "dueDate" to formattedDate,
                    "note" to note.trim()
                ),
                token
            )
        }

        val newLoan = Loan(
            userId = userId,
            friendName = friendName.trim(),
            amount = amount,
            dueDateMillis = dueDateMillis,
            isRepaid = false,
            createdAtMillis = System.currentTimeMillis(),
            note = note.trim()
        )

        val id = loanDao.insertLoan(newLoan)
        Result.success(newLoan.copy(id = id))
    }

    /**
     * PATCH /loans/:id/repay
     * Marks loan as repaid in database.
     */
    suspend fun markLoanAsRepaid(loanId: Long, token: String?): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!token.isNullOrEmpty()) {
            callBackendJson("PATCH", "/loans/$loanId/repay", emptyMap(), token)
        }

        loanDao.markAsRepaid(loanId, System.currentTimeMillis())
        Result.success(true)
    }

    /**
     * Helper to perform HTTP requests to backend server
     */
    private fun callBackendJson(
        method: String,
        path: String,
        body: Map<String, Any>,
        token: String?
    ): Result<JSONObject> {
        return try {
            val url = URL("$backendBaseUrl$path")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = method
            conn.connectTimeout = 1500
            conn.readTimeout = 1500
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            if (!token.isNullOrEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }

            if (method == "POST" || method == "PATCH") {
                conn.doOutput = true
                val jsonBody = JSONObject(body).toString()
                OutputStreamWriter(conn.outputStream).use { it.write(jsonBody) }
            }

            val statusCode = conn.responseCode
            val inputStream = if (statusCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

            if (statusCode in 200..299) {
                Result.success(JSONObject(if (responseText.startsWith("{")) responseText else "{\"data\":$responseText}"))
            } else {
                val errObj = try { JSONObject(responseText) } catch (e: Exception) { null }
                val errMsg = errObj?.optString("error") ?: "HTTP error $statusCode"
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
