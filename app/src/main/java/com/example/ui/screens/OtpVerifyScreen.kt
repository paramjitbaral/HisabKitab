package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MiniLedgerUiState
import com.example.ui.components.ConsoleOtpBanner
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.OutlineMuted
import com.example.ui.theme.OverdueRedText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmBackground

@Composable
fun OtpVerifyScreen(
    uiState: MiniLedgerUiState,
    onDigitChange: (Int, String) -> Unit,
    onAutoFill: () -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }



    Box(modifier = modifier.fillMaxSize().background(WarmBackground)) {
        Image(painterResource(R.drawable.otp_interior), null,
            Modifier.matchParentSize(), contentScale = ContentScale.Crop)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
    ) {
        // Top App Bar with Back Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("otp_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(56.dp))

        // Center Content Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Verify your number",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We've sent a 6-digit code to\n${uiState.formattedFullPhone}",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 6 OTP Digit Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val digit = uiState.otpDigits.getOrElse(i) { "" }
                    val isFocused = (uiState.otpDigits.indexOfFirst { it.isEmpty() }.let { if (it == -1) 5 else it } == i)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { focusRequester.requestFocus() }
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) ForestGreenPrimary else OutlineMuted,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("otp_box_$i"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Hidden or connected invisible text input field to catch keyboard input smoothly
            Spacer(modifier = Modifier.height(8.dp))
            BasicTextField(
                value = uiState.otpDigits.joinToString(""),
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(6)
                    for (i in 0 until 6) {
                        val char = if (i < filtered.length) filtered[i].toString() else ""
                        onDigitChange(i, char)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = { onVerify() }),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .size(1.dp)
                    .testTag("otp_hidden_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Check the server console for the OTP\n(this is a mock OTP)",
                modifier = Modifier.clickable(onClickLabel = "Fill mock OTP") { onAutoFill() },
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            // Auth Error
            if (!uiState.authError.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.authError,
                    color = OverdueRedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Verify OTP Button
            Button(
                onClick = onVerify,
                enabled = !uiState.isVerifyingOtp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("verify_otp_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreenPrimary,
                    disabledContainerColor = Color(0xFF4A6857)
                )
            ) {
                if (uiState.isVerifyingOtp) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verify OTP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resend OTP Countdown / Action
            if (uiState.canResend) {
                Text(
                    text = "Resend OTP",
                    color = ForestGreenPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onResend() }
                        .padding(8.dp)
                        .testTag("resend_otp_button")
                )
            } else {
                Text(
                    text = "Resend OTP in ${uiState.countdownSeconds}s",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
}
