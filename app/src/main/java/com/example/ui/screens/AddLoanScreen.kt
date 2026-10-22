package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.example.R
import com.example.ui.*
import com.example.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddLoanScreen(uiState: MiniLedgerUiState, onFriendNameChange: (String)->Unit,
    onAmountChange: (String)->Unit, onDueDateChange: (Long)->Unit, onSubmit: ()->Unit,
    onBack: ()->Unit, onNoteChange: (String)->Unit = {}, onNavigate: (Screen)->Unit = {},
    modifier: Modifier = Modifier) {
    var datePicker by remember { mutableStateOf(false) }
    val date = uiState.newDueDateMillis?.let { SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(it)) }.orEmpty()
    Scaffold(modifier=modifier.imePadding(), containerColor=LedgerPaper, contentWindowInsets=WindowInsets(0,0,0,0),
        bottomBar={LedgerNav(Screen.AddLoan,onNavigate)}) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val canvasHeight = maxHeight.coerceAtLeast(740.dp)
            val canvasWidth = maxWidth
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(Modifier.fillMaxWidth().height(canvasHeight)) {
                    Image(painterResource(R.drawable.add_loan_clipped),null,Modifier.matchParentSize(),contentScale=ContentScale.FillBounds)
                    Column {
                Box(Modifier.fillMaxWidth().height(canvasHeight * .42f)) {
                    LedgerPhotoHeading("Add New Loan","A small help today,\na stronger bond tomorrow.",onBack,Modifier.testTag("add_loan_back_button"))
                }
                Surface(color=Color.Transparent) {
                    Column(Modifier.fillMaxWidth().padding(horizontal=20.dp)) {
                        LoanFormField(Icons.Outlined.Person,"Friend’s Name") {
                            ReferenceInput(uiState.newFriendName,onFriendNameChange,"Enter friend’s name",Modifier.testTag("friend_name_input"),KeyboardOptions(capitalization=KeyboardCapitalization.Words))
                        }
                        Spacer(Modifier.height(10.dp))
                        LoanFormField(Icons.Outlined.CurrencyRupee,"Amount (₹)") {
                            ReferenceInput(uiState.newAmount,onAmountChange,"Enter amount",Modifier.testTag("amount_input"),KeyboardOptions(keyboardType=KeyboardType.Decimal))
                            Row(Modifier.fillMaxWidth().padding(top=8.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                listOf(500,1000,2000,5000).forEach { amount ->
                                    Surface(onClick={onAmountChange(amount.toString())},shape=RoundedCornerShape(20.dp),
                                        color=if(uiState.newAmount==amount.toString()) Color(0xFFD7E8DD) else Color(0xFFF2F4F3),modifier=Modifier.weight(1f)) {
                                        Box(Modifier.padding(vertical=6.dp),contentAlignment=Alignment.Center) { Text("₹${amount}",fontSize=11.sp,lineHeight=14.sp,color=LedgerInk) }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        LoanFormField(Icons.Outlined.CalendarMonth,"Due Date") {
                            Surface(onClick={datePicker=true},shape=RoundedCornerShape(9.dp),color=Color.White,
                                border=BorderStroke(1.dp,Color(0xFFE1E5E3)),modifier=Modifier.fillMaxWidth().height(38.dp).testTag("due_date_picker_button")) {
                                Row(Modifier.padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically) {
                                    Text(date.ifEmpty { "Select due date" },color=if(date.isEmpty()) LedgerMuted else LedgerInk,fontSize=12.sp,modifier=Modifier.weight(1f))
                                    Icon(Icons.Outlined.CalendarMonth,"Pick date",tint=LedgerInk,modifier=Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        LoanFormField(Icons.Outlined.Description,"Note (Optional)") {
                            ReferenceInput(uiState.newNote,onNoteChange,"Add a short note...",Modifier.testTag("note_input"))
                        }
                        uiState.addLoanError?.let { Text(it,color=Color(0xFFB2382F),fontSize=12.sp,modifier=Modifier.padding(top=6.dp)) }
                        Spacer(Modifier.height(10.dp))
                        LedgerSaveButton("Save Loan",uiState.isAddingLoan,onSubmit,Modifier.testTag("save_loan_button"))
                    }
                }
            }
        }
    }
    }
    }
    if(datePicker) DatePickerModal(onDateSelected={it?.let(onDueDateChange)},onDismiss={datePicker=false})
}

@Composable
private fun LoanFormField(icon: androidx.compose.ui.graphics.vector.ImageVector, label:String, content:@Composable ColumnScope.()->Unit) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Top) {
        Box(Modifier.padding(top=2.dp).size(30.dp).background(Color(0xFFF2F4F3),CircleShape),contentAlignment=Alignment.Center) {
            Icon(icon,null,tint=LedgerInk,modifier=Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) { Text(label,color=LedgerInk,fontSize=12.sp,lineHeight=16.sp);Spacer(Modifier.height(4.dp));content() }
    }
}

@Composable
private fun formColors() = OutlinedTextFieldDefaults.colors(focusedContainerColor=Color.White,unfocusedContainerColor=Color.White,
    focusedBorderColor=LedgerGreen,unfocusedBorderColor=Color(0xFFE1E5E3),focusedTextColor=LedgerInk,unfocusedTextColor=LedgerInk)

@Composable
private fun ReferenceInput(value:String,onChange:(String)->Unit,placeholder:String,modifier:Modifier=Modifier,keyboard:KeyboardOptions=KeyboardOptions.Default) {
    BasicTextField(value,onChange,modifier.fillMaxWidth().height(38.dp),singleLine=true,
        textStyle=TextStyle(color=LedgerInk,fontSize=12.sp),keyboardOptions=keyboard,cursorBrush=SolidColor(LedgerGreen),
        decorationBox={ inner ->
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(9.dp)).background(Color.White)
                .border(1.dp,Color(0xFFE1E5E3),RoundedCornerShape(9.dp)).padding(horizontal=14.dp),contentAlignment=Alignment.CenterStart) {
                if(value.isEmpty()) Text(placeholder,color=LedgerMuted,fontSize=12.sp)
                inner()
            }
        })
}

