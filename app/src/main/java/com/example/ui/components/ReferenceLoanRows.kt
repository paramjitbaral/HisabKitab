package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.R
import com.example.data.model.Loan

@Composable
fun CompactLoanRow(loan:Loan,onClick:()->Unit,modifier:Modifier=Modifier,history:Boolean=false) {
    val overdue=loan.isOverdue()
    val status=if(loan.isRepaid) "Repaid" else if(overdue) "Overdue" else "Pending"
    val accent=if(loan.isRepaid) Color(0xFF08633F) else if(overdue) Color(0xFFAA2820) else LedgerInk
    val avatar=if(loan.isRepaid) Color(0xFFCCE7D2) else if(overdue) Color(0xFFFFD1CA) else Color(0xFFE2E1E5)
    Surface(onClick=onClick,modifier=modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color(0xFFFBF7F1)) {
        Row(Modifier.padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(if(history) 36.dp else 46.dp).background(avatar,CircleShape),contentAlignment=Alignment.Center) {
                Text(loan.friendName.take(1).uppercase(),color=accent,fontSize=if(history) 19.sp else 23.sp,fontWeight=FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(loan.friendName,fontSize=14.sp,lineHeight=18.sp,color=LedgerInk,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(money(loan.amount),fontSize=14.sp,lineHeight=18.sp,fontWeight=FontWeight.SemiBold,color=LedgerInk)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth,null,tint=LedgerMuted,modifier=Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if(history && loan.note.isNotBlank()) loan.note else if(loan.isRepaid) loan.formattedRepaidDate() else "Due ${loan.formattedDueDate()}",fontSize=10.sp,lineHeight=14.sp,color=LedgerMuted,maxLines=1)
                }
            }
            SmallStatus(status)
            Icon(Icons.Outlined.ChevronRight,"Loan details",tint=LedgerMuted,modifier=Modifier.size(18.dp))

        }
    }
}

@Composable
fun SmallStatus(status:String,modifier:Modifier=Modifier) {
    val background=when(status){"Repaid"->Color(0xFFCAE8D5);"Overdue"->Color(0xFFFFD1CB);else->Color(0xFFE8E5E0)}
    val color=when(status){"Repaid"->Color(0xFF07583A);"Overdue"->Color(0xFFB7362C);else->LedgerInk}
    Text(status,modifier.background(background,RoundedCornerShape(20.dp)).padding(horizontal=10.dp,vertical=5.dp),color=color,fontSize=10.sp,lineHeight=13.sp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailsDialog(loan:Loan,onDismiss:()->Unit,onRepaid:()->Unit) {
    var submitted by remember(loan.id) { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest=onDismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=Color(0xFFFFFCF7),shape=RoundedCornerShape(topStart=22.dp,topEnd=22.dp),tonalElevation=0.dp) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=24.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Text("Loan details",fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=LedgerInk,modifier=Modifier.weight(1f))
                IconButton(onClick=onDismiss){Icon(Icons.Outlined.Close,"Close loan details",tint=LedgerMuted,modifier=Modifier.size(20.dp))}
            }
            Text(loan.friendName,fontSize=14.sp,color=LedgerMuted,modifier=Modifier.padding(top=8.dp))
            Text(money(loan.amount),fontSize=32.sp,fontWeight=FontWeight.SemiBold,color=LedgerInk,modifier=Modifier.padding(top=4.dp,bottom=20.dp))
            LoanDetailLine("Status",if(loan.isRepaid) "Repaid" else if(loan.isOverdue()) "Overdue" else "Pending")
            LoanDetailLine("Due date",loan.formattedDueDate())
            if(loan.isRepaid) LoanDetailLine("Repaid on",loan.formattedRepaidDate())
            if(loan.note.isNotBlank()) { Text("Note",fontSize=12.sp,color=LedgerMuted,modifier=Modifier.padding(top=18.dp));Text(loan.note,fontSize=14.sp,color=LedgerInk,modifier=Modifier.padding(top=6.dp)) }
            if(!loan.isRepaid) {
                Text("Record this loan as fully repaid once you’ve received the money.",fontSize=12.sp,lineHeight=18.sp,color=LedgerMuted,modifier=Modifier.padding(top=22.dp,bottom=14.dp))
                Button(onClick={submitted=true;onRepaid()},enabled=!submitted,shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=LedgerGreen),modifier=Modifier.fillMaxWidth().height(48.dp)) { Text(if(submitted) "Saving…" else "Mark as repaid",fontSize=14.sp) }
            }
        }
    }
}

@Composable
private fun LoanDetailLine(label:String,value:String) {
    HorizontalDivider(color=LedgerInk.copy(alpha=.08f))
    Row(Modifier.fillMaxWidth().padding(vertical=14.dp),horizontalArrangement=Arrangement.spacedBy(18.dp)) {
        Text(label,color=LedgerMuted,fontSize=13.sp,modifier=Modifier.weight(1f))
        Text(value,color=LedgerInk,fontSize=13.sp,modifier=Modifier.weight(1f),textAlign=androidx.compose.ui.text.style.TextAlign.End)
    }
}
