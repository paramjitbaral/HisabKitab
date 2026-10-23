package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.unit.*
import com.example.data.model.Loan
import com.example.ui.Screen
import com.example.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(loans:List<Loan>,onNavigate:(Screen)->Unit,onMarkAsRepaid:(Loan)->Unit,modifier:Modifier=Modifier,activeLoansOnly:Boolean=false) {
    var filter by remember { mutableStateOf("All") }
    var showDate by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var query by remember { mutableStateOf("") }
    var search by remember { mutableStateOf(false) }
    var newestFirst by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<Loan?>(null) }
    val filtered=loans.filter { loan ->
        (!activeLoansOnly || !loan.isRepaid) &&
        (selectedDate == null || SimpleDateFormat("yyyyMMdd",Locale.US).format(Date(loan.historyDate())) == SimpleDateFormat("yyyyMMdd",Locale.US).format(Date(selectedDate!!))) &&
        (when(filter){"Pending"->!loan.isRepaid&&!loan.isOverdue();"Repaid"->loan.isRepaid;"Overdue"->loan.isOverdue();else->true}) &&
            (loan.friendName.contains(query,true)||loan.note.contains(query,true))
    }.let { list -> if(newestFirst) list.sortedByDescending { it.historyDate() } else list.sortedBy { it.historyDate() } }
    val groups=filtered.groupBy { SimpleDateFormat("MMM yyyy",Locale.getDefault()).format(Date(it.historyDate())).uppercase() }
    Scaffold(modifier=modifier,containerColor=LedgerPaper,contentWindowInsets=WindowInsets(0,0,0,0),
        bottomBar={LedgerNav(if(activeLoansOnly) Screen.Loans else Screen.History,onNavigate)}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(Brush.verticalGradient(listOf(Color(0xFF6E655A),LedgerPaper)))) {
            Box {
            Image(painterResource(R.drawable.onboarding_ledger), null, Modifier.matchParentSize().graphicsLayer { scaleY=1.25f;transformOrigin=TransformOrigin(.5f,0f) }, contentScale=ContentScale.Crop)
            Box(Modifier.matchParentSize().graphicsLayer { scaleY=1.25f;transformOrigin=TransformOrigin(.5f,0f) }.background(Color.Black.copy(alpha=.32f)))
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=22.dp).padding(top=12.dp,bottom=30.dp)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    RoundAction(Icons.AutoMirrored.Outlined.ArrowBack,"Back",{onNavigate(Screen.Dashboard)},Modifier.size(36.dp))
                    Spacer(Modifier.weight(1f))
                    RoundAction(Icons.Outlined.CalendarMonth,"Filter by date",{showDate=true},Modifier.size(36.dp))
                }
                Spacer(Modifier.height(12.dp))
                LedgerTitle(if(activeLoansOnly) "My Loans" else "Loan History",color=Color.White,size=29)
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(if(activeLoansOnly) "Keep track of outstanding loans." else "All your lending activity\nin one place.",color=Color.White,fontSize=13.sp,lineHeight=18.sp,modifier=Modifier.weight(1f))
                    IconButton(onClick={search=!search;if(!search)query=""}){Icon(Icons.Outlined.Search,"Search loans",tint=Color.White,modifier=Modifier.size(18.dp))}
                }
            }
            }
            Surface(Modifier.fillMaxSize(),shape=RoundedCornerShape(topStart=30.dp,topEnd=30.dp),color=LedgerPaper) {
                Column(Modifier.padding(horizontal=16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                        listOf("All","Pending","Repaid","Overdue").forEach { label ->
                            Surface(onClick={filter=label},shape=RoundedCornerShape(30.dp),color=if(filter==label) LedgerGreen else Color(0xFFEDE7DF),modifier=Modifier.weight(1f)) {
                                Box(Modifier.padding(vertical=7.dp),contentAlignment=Alignment.Center) { Text(label,color=if(filter==label) Color.White else LedgerInk,fontSize=10.sp,lineHeight=14.sp) }
                            }
                        }
                    }
                    if(selectedDate!=null) TextButton(onClick={selectedDate=null}) { Text("Clear date filter",color=LedgerGreen) }
                    if(search) OutlinedTextField(query,{query=it},Modifier.fillMaxWidth().padding(bottom=12.dp),singleLine=true,
                        placeholder={Text("Search names or notes")},shape=RoundedCornerShape(20.dp))
                    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=16.dp)) {
                        if(filtered.isEmpty()) item {
                            Column(Modifier.fillMaxWidth().padding(vertical=48.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.History,null,tint=LedgerMuted,modifier=Modifier.size(40.dp))
                                Spacer(Modifier.height(12.dp));LedgerTitle(if(loans.isEmpty()) "Your story starts here." else "No matching loans.",size=21)
                                Text(if(loans.isEmpty()) "Every loan you add will appear here." else "Try another filter or search.",color=LedgerMuted,fontSize=13.sp,modifier=Modifier.padding(top=8.dp))
                            }
                        }
                        groups.forEach { (month, entries) ->
                            item { Text(month,color=Color(0xFF58636A),fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.padding(top=8.dp,bottom=10.dp)) }
                            items(entries,key={it.id}) { loan ->
                                Row(Modifier.fillMaxWidth().padding(bottom=8.dp),verticalAlignment=Alignment.CenterVertically) {
                                    Row(Modifier.width(42.dp),verticalAlignment=Alignment.CenterVertically) {
                                        Column(Modifier.width(28.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                            Text(SimpleDateFormat("dd",Locale.getDefault()).format(Date(loan.historyDate())),color=if(loan.isOverdue()) Color(0xFFC63930) else LedgerInk,fontSize=14.sp,lineHeight=18.sp)
                                            Text(SimpleDateFormat("MMM",Locale.getDefault()).format(Date(loan.historyDate())),color=LedgerMuted,fontSize=9.sp,lineHeight=12.sp)
                                        }
                                        Box(Modifier.width(10.dp).height(64.dp),contentAlignment=Alignment.Center) {
                                            Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFD2CCC4)))
                                            Box(Modifier.size(6.dp).background(LedgerPaper,CircleShape).border(1.dp,if(loan.isOverdue()) Color(0xFFED5B52) else LedgerMuted,CircleShape))
                                        }
                                    }
                                    Spacer(Modifier.width(4.dp));CompactLoanRow(loan,{selected=loan},Modifier.weight(1f),history=true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if(showDate) DatePickerModal(onDateSelected={selectedDate=it},onDismiss={showDate=false})
    selected?.let { loan -> LoanDetailsDialog(loan,{selected=null},{onMarkAsRepaid(loan);selected=null}) }
}
private fun Loan.historyDate():Long = if(isRepaid) repaidDateMillis ?: dueDateMillis else dueDateMillis
