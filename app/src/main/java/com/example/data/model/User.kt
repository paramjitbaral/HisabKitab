package com.example.data.model

data class UserSession(
    val phoneNumber: String,
    val name: String,
    val token: String
)

data class OtpRequestState(
    val phoneNumber: String = "",
    val generatedOtp: String = "",
    val remainingSeconds: Int = 30,
    val isSending: Boolean = false,
    val error: String? = null
)
