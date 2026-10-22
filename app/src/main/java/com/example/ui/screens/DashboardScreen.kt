package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.R
import com.example.data.model.*
import com.example.ui.*
import com.example.ui.components.*
import java.util.Calendar

@Composable
fun DashboardScreen(uiState: MiniLedgerUiState, loans: List<Loan>, summary: LoanSummary,
    onNavigate: (Screen) -> Unit, onMarkAsRepaid: (Loan) -> Unit, onLogout: () -> Unit,
    onClearFeedback: () -> Unit, modifier: Modifier = Modifier) {
    var selectedLoan by remember { mutableStateOf<Loan?>(null) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.feedbackMessage) { uiState.feedbackMessage?.let { snackbar.showSnackbar(it); onClearFeedback() } }
    val name = uiState.currentUser?.name?.takeIf { it != "User" && it.any(Char::isLetter) }
    val greeting = when(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) { in 0..11 -> "Good morning,"; in 12..16 -> "Good afternoon,"; else -> "Good evening," }
    val upcoming = loans.filter { !it.isRepaid }.sortedBy { it.dueDateMillis }.take(2)
    Scaffold(modifier = modifier.testTag("dashboard_screen"), containerColor = LedgerPaper,
        contentWindowInsets = WindowInsets(0,0,0,0), snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { LedgerNav(Screen.Dashboard, onNavigate) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Box(Modifier.fillMaxWidth().height(500.dp)) {
                    Image(painterResource(R.drawable.home_cafe), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.32f), Color.Transparent, Color.Transparent))))
                    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=22.dp, vertical=12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoStories, null, tint=Color.White, modifier=Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp)); LedgerTitle("HisabKitab", color=Color.White, size=19)
                            Spacer(Modifier.weight(1f))
                            Surface(onClick={onNavigate(Screen.Settings)}, shape=CircleShape, color=Color.White, modifier=Modifier.size(36.dp).testTag("profile_avatar_button")) {
                                ProfileAvatar(uiState.profilePhoto, name, Modifier.fillMaxSize())
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        LedgerTitle(greeting, color=Color.White, size=21)
                        LedgerTitle("${name ?: "Hello there"} 👋", color=Color.White, size=28)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                            Text("Keep track of what you lend,\nin a simple way.", color=Color.White, fontSize=14.sp, lineHeight=20.sp, modifier=Modifier.weight(1f))
                            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                Surface(onClick={onNavigate(Screen.AddLoan)}, shape=CircleShape, color=LedgerGreen,
                                    border=BorderStroke(1.dp, Color.White), modifier=Modifier.size(46.dp).testTag("fab_add_loan")) {
                                    Box(contentAlignment=Alignment.Center) { Icon(Icons.Outlined.Add, "Add Loan", tint=Color.White, modifier=Modifier.size(24.dp)) }
                                }
                                Text("Add Loan", color=Color.White, fontSize=11.sp, modifier=Modifier.padding(top=5.dp))
                            }
                        }
                    }
                    SummaryCard(summary, loans, Modifier.align(Alignment.BottomCenter).padding(start=18.dp,end=18.dp,top=12.dp,bottom=72.dp))
                    Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(60.dp),color=LedgerPaper,
                        shape=RoundedCornerShape(topStart=28.dp,topEnd=28.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal=22.dp, vertical=0.dp), verticalAlignment=Alignment.CenterVertically) {
                    LedgerTitle("Upcoming", size=18); Spacer(Modifier.weight(1f))
                    TextButton(onClick={onNavigate(Screen.History)}) { Text("View all", color=LedgerMuted); Icon(Icons.Outlined.ArrowForward, null, tint=LedgerMuted, modifier=Modifier.size(18.dp)) }
                }
                    }
                }
            }
            if (upcoming.isEmpty()) item {
                Surface(Modifier.fillMaxWidth().padding(horizontal=20.dp, vertical=4.dp), shape=RoundedCornerShape(20.dp), color=Color.White,
                    border=BorderStroke(1.dp,Color(0xFFDDE4E0))) {
                    Column(Modifier.padding(16.dp)) {
                        LedgerTitle(if(loans.isEmpty()) "Your ledger starts here." else "All caught up.", size=18)
                        Text(if(loans.isEmpty()) "Add your first loan to keep it all in one place." else "No repayments waiting. View your history anytime.", color=LedgerMuted, fontSize=13.sp, modifier=Modifier.padding(top=6.dp))
                    }
                }
            }
            items(upcoming, key={it.id}) { loan ->
                CompactLoanRow(loan, { selectedLoan=loan }, Modifier.padding(horizontal=20.dp, vertical=3.dp))
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
    selectedLoan?.let { loan -> LoanDetailsDialog(loan, {selectedLoan=null}, {onMarkAsRepaid(loan);selectedLoan=null}) }

}

@Composable
fun SummaryCard(summary: LoanSummary, loans: List<Loan> = emptyList(), modifier: Modifier = Modifier) {
    val now = Calendar.getInstance()
    val monthStart = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH,1);set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0) }
    val previousStart = (monthStart.clone() as Calendar).apply { add(Calendar.MONTH,-1) }.timeInMillis
    val thisMonth = loans.filter { it.createdAtMillis >= monthStart.timeInMillis }.sumOf { it.amount }
    val lastMonth = loans.filter { it.createdAtMillis in previousStart until monthStart.timeInMillis }.sumOf { it.amount }
    val trend = if(lastMonth > 0) { val change=((thisMonth-lastMonth)/lastMonth*100).toInt(); "${if(change>=0) "↑" else "↓"} ${kotlin.math.abs(change)}% from last month" } else "Your lending, all in one place"
    Column(modifier.fillMaxWidth().clip(LedgerSummaryShape).background(LedgerGradient).padding(start=20.dp, end=20.dp, top=32.dp, bottom=18.dp).testTag("summary_card")) {
        Text("Total Lent", color=Color.White, fontSize=12.sp, lineHeight=16.sp)
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text(money(summary.totalLent), color=Color.White, fontSize=27.sp, lineHeight=32.sp, fontWeight=FontWeight.SemiBold, modifier=Modifier.weight(1f))
            Icon(Icons.Outlined.BarChart, null, tint=Color.White, modifier=Modifier.size(24.dp))
        }
        Text(trend, color=Color.White.copy(alpha=.85f), fontSize=10.sp, lineHeight=14.sp)
        Spacer(Modifier.height(12.dp))
        Canvas(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(9.dp))) {
            drawRoundRect(Color.White.copy(alpha=.12f), cornerRadius=androidx.compose.ui.geometry.CornerRadius(size.height/2))
            if(summary.totalLent>0) {
                val paid=(summary.totalRepaid/summary.totalLent).coerceIn(0.0,1.0).toFloat()
                val overdue=(summary.overdueAmount/summary.totalLent).coerceIn(0.0,1.0-paid.toDouble()).toFloat()
                drawRect(Color(0xFF52CA96), size=androidx.compose.ui.geometry.Size(size.width*paid,size.height))
                drawRect(Color(0xFFFF746B), topLeft=androidx.compose.ui.geometry.Offset(size.width*paid,0f), size=androidx.compose.ui.geometry.Size(size.width*overdue,size.height))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) { Text("●  ${money(summary.totalRepaid)}", color=Color.White, fontSize=14.sp, lineHeight=18.sp); Text("    Repaid", color=Color(0xFF77E0AA), fontSize=12.sp, lineHeight=16.sp) }
            Column(Modifier.weight(1f)) { Text("●  ${money(summary.overdueAmount)}", color=Color.White, fontSize=14.sp, lineHeight=18.sp); Text("    Overdue", color=Color(0xFFFF796F), fontSize=12.sp, lineHeight=16.sp) }
        }
    }
}

// The reference card rises at the left and sweeps down toward the right.
private val LedgerSummaryShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val r = size.width * .075f
    moveTo(0f, r * 1.7f)
    cubicTo(0f, r * .45f, r * .55f, 0f, r * 1.65f, 0f)
    cubicTo(w * .38f, 0f, w * .67f, r * 1.05f, w - r, r * 1.15f)
    quadraticBezierTo(w, r * 1.25f, w, r * 2.3f)
    lineTo(w, h - r)
    quadraticBezierTo(w, h, w - r, h)
    lineTo(r, h)
    quadraticBezierTo(0f, h, 0f, h - r)
    close()
}
