package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.OverdueRedBg
import com.example.ui.theme.OverdueRedText
import com.example.ui.theme.PendingGrayBg
import com.example.ui.theme.PendingGrayText
import com.example.ui.theme.RepaidGreenBg
import com.example.ui.theme.RepaidGreenText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmBackground

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        "Overdue" -> OverdueRedBg to OverdueRedText
        "Repaid" -> RepaidGreenBg to RepaidGreenText
        else -> PendingGrayBg to PendingGrayText
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ConsoleOtpBanner(
    otp: String?,
    onAutoFill: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !otp.isNullOrEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("console_otp_banner"),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E2822),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C3E34)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Server Log",
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Server Console Log",
                            fontSize = 11.sp,
                            color = Color(0xFF9EBAA9),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Mock OTP: $otp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6EE7B7))
                        .clickable { onAutoFill() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("auto_fill_otp_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Auto-fill",
                        color = Color(0xFF0F2618),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MiniLedgerBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar"),
        containerColor = WarmBackground,
        tonalElevation = 6.dp
    ) {
        val isHome = currentScreen == Screen.Dashboard
        val isAdd = currentScreen == Screen.AddLoan

        NavigationBarItem(
            selected = isHome,
            onClick = { onNavigate(Screen.Dashboard) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = if (isHome) ForestGreenPrimary else TextMuted
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHome) ForestGreenPrimary else TextMuted
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFE2EBE5)
            ),
            modifier = Modifier.testTag("nav_home_tab")
        )

        NavigationBarItem(
            selected = isAdd,
            onClick = { onNavigate(Screen.AddLoan) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.PostAdd,
                    contentDescription = "Add Loan",
                    tint = if (isAdd) ForestGreenPrimary else TextMuted
                )
            },
            label = {
                Text(
                    text = "Add Loan",
                    fontWeight = if (isAdd) FontWeight.Bold else FontWeight.Normal,
                    color = if (isAdd) ForestGreenPrimary else TextMuted
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFE2EBE5)
            ),
            modifier = Modifier.testTag("nav_add_loan_tab")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                },
                modifier = Modifier.testTag("date_picker_confirm_button")
            ) {
                Text("OK", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
