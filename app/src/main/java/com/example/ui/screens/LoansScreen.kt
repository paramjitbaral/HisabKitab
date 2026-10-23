package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import com.example.data.model.Loan
import com.example.ui.Screen
import com.example.ui.components.*

@Composable
fun LoansScreen(loans: List<Loan>, onNavigate: (Screen)->Unit, onMarkAsRepaid: (Loan)->Unit) {
    var overdueOnly by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Loan?>(null) }
    val open = loans.filter { !it.isRepaid }
    val people = open.filter { !overdueOnly || it.isOverdue() }.groupBy { it.friendName.trim().lowercase() }
        .values.sortedBy { group -> group.minOf { it.dueDateMillis } }
    Scaffold(containerColor=LedgerPaper,contentWindowInsets=WindowInsets(0,0,0,0), bottomBar={LedgerNav(Screen.Loans,onNavigate)}) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(bottom=18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(204.dp)) {
                    Image(painterResource(R.drawable.dashboard_wall),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
                    Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.38f)))
                    RoundAction(Icons.AutoMirrored.Outlined.ArrowBack,"Back",{onNavigate(Screen.Dashboard)},Modifier.statusBarsPadding().padding(start=16.dp,top=8.dp))
                    Column(Modifier.align(Alignment.BottomStart).padding(horizontal=20.dp).padding(bottom=42.dp)) {
                        Text("YOUR LENDING",color=Color.White,fontSize=10.sp,letterSpacing=2.sp)
                        LedgerTitle("Outstanding",size=27,color=Color.White)
                        Text("Balances to collect, organised by person.",color=Color.White,fontSize=11.sp)
                    }
                    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(26.dp).background(LedgerPaper,RoundedCornerShape(topStart=26.dp,topEnd=26.dp)))
                }
            }
            item {
                Surface(color=LedgerGreen,shape=RoundedCornerShape(18.dp),modifier=Modifier.padding(horizontal=18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Still to receive",color=Color.White.copy(alpha=.7f),fontSize=11.sp)
                        LedgerTitle(money(open.sumOf { it.amount }),color=Color.White,size=27)
                        Spacer(Modifier.height(6.dp))
                        Text("${open.size} open loans  ·  ${open.map { it.friendName.trim().lowercase() }.distinct().size} people",color=Color.White,fontSize=11.sp)
                        Text("${money(open.filter { it.isOverdue() }.sumOf { it.amount })} overdue",color=Color(0xFFFFB5A7),fontSize=11.sp)
                    }
                }
            }
            item { Row(Modifier.padding(horizontal=22.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                FilterChip(selected=!overdueOnly,onClick={overdueOnly=false},label={Text("Everyone",fontSize=11.sp)},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=LedgerGreen,selectedLabelColor=Color.White,labelColor=LedgerMuted))
                FilterChip(selected=overdueOnly,onClick={overdueOnly=true},label={Text("Needs attention",fontSize=11.sp)},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=LedgerGreen,selectedLabelColor=Color.White,labelColor=LedgerMuted))
            } }
            if(people.isEmpty()) item {
                Column(Modifier.padding(horizontal=22.dp)) {
                LedgerTitle(if(open.isEmpty()) "All settled up." else "Nothing overdue.",size=24)
                Text("New outstanding balances will appear here.",color=LedgerMuted,fontSize=13.sp)
                TextButton(onClick={onNavigate(Screen.AddLoan)}) { Text("Add a loan",color=LedgerGreen) }
                }
            }
            items(people,key={it.first().friendName.trim().lowercase()}) { group ->
                Surface(color=Color(0xFFFFFCF7),shape=RoundedCornerShape(18.dp),modifier=Modifier.padding(horizontal=22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                LedgerTitle(group.first().friendName,size=18)
                                Text("${group.size} open loan${if(group.size==1) "" else "s"}",color=LedgerMuted,fontSize=11.sp)
                            }
                            Text(money(group.sumOf { it.amount }),color=LedgerGreen,fontSize=17.sp)
                        }
                        group.sortedBy { it.dueDateMillis }.forEach { loan ->
                            HorizontalDivider(Modifier.padding(vertical=6.dp),color=LedgerPaper)
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text(money(loan.amount),color=LedgerInk,fontSize=14.sp)
                                    Text("${if(loan.isOverdue()) "Overdue" else "Due"} · ${loan.formattedDueDate()}",color=if(loan.isOverdue()) Color(0xFFAD4635) else LedgerMuted,fontSize=11.sp)
                                }
                                TextButton(onClick={selected=loan}) { Text("Manage",color=LedgerGreen,fontSize=12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
    selected?.let { LoanDetailsDialog(it,{selected=null},{onMarkAsRepaid(it);selected=null}) }
}
