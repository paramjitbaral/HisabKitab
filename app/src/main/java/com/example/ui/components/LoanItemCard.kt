package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Loan
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.OverdueRedBg
import com.example.ui.theme.OverdueRedText
import com.example.ui.theme.RepaidGreenBg
import com.example.ui.theme.RepaidGreenText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoanItemCard(
    loan: Loan,
    onMarkAsRepaid: (Loan) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember(loan.id) { mutableStateOf(false) }
    val isOverdue = loan.isOverdue()
    val isRepaid = loan.isRepaid

    val statusText = when {
        isRepaid -> "Repaid"
        isOverdue -> "Overdue"
        else -> "Pending"
    }

    // Avatar color scheme based on status
    val (avatarBg, avatarText) = when {
        isRepaid -> RepaidGreenBg to RepaidGreenText
        isOverdue -> Color(0xFFF8D1C8) to OverdueRedText
        else -> Color(0xFFE4E8E6) to TextPrimary
    }

    val cardBg = when {
        isOverdue -> Color(0xFFFAEAE5)
        isRepaid -> Color(0xFFEDF4ED)
        else -> Color(0xFFFFFFFF)
    }

    val cardBorder = when {
        isOverdue -> Color(0xFFFFD4D4)
        else -> Color(0xFFE4E8E6)
    }

    val firstLetter = loan.friendName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val formattedAmount = NumberFormat.getNumberInstance(Locale.getDefault()).format(loan.amount.toLong())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .testTag("loan_card_${loan.id}")
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Friend initial
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstLetter,
                        color = avatarText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.friendName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹ $formattedAmount",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) OverdueRedText else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (isOverdue) OverdueRedText else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRepaid) "Repaid on ${loan.formattedRepaidDate()}" else "Due ${loan.formattedDueDate()}",
                            fontSize = 12.sp,
                            color = if (isOverdue) OverdueRedText else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Pill
                StatusBadge(status = statusText)
                Icon(Icons.Default.KeyboardArrowRight, if (expanded) "Collapse loan" else "Show loan actions",
                    modifier = Modifier.size(20.dp), tint = TextSecondary)
            }

            // Mark as Repaid Action Button for unpaid entries
            if (!isRepaid && expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onMarkAsRepaid(loan) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isOverdue) Color(0xFFFFF0F0) else Color(0xFFF4F8F5),
                            contentColor = ForestGreenPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isOverdue) Color(0xFFFFC5C5) else Color(0xFFC7DEC9)
                            )
                        ),
                        modifier = Modifier.testTag("mark_repaid_button_${loan.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ForestGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark as Repaid",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenPrimary
                        )
                    }
                }
            }
        }
    }
}
