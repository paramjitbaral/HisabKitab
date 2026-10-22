package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.ui.MiniLedgerUiState
import com.example.ui.Screen
import com.example.ui.components.*

@Composable
fun SettingsDetailPage(title:String,state:MiniLedgerUiState,onBack:()->Unit,onSaveName:(String)->Unit,onChoosePhoto:()->Unit,onPhotoChange:(String?)->Unit,onLogout:()->Unit,onIntroduction:()->Unit,onNavigate:(Screen)->Unit,loans:List<com.example.data.model.Loan> = emptyList(),onRestore:suspend (List<com.example.data.model.Loan>)->Int={0},onErase:suspend ()->Unit={}) {
    val context=LocalContext.current
    var name by rememberSaveable { mutableStateOf(state.currentUser?.name?.takeUnless { it=="User" }.orEmpty()) }
    var saved by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<List<com.example.data.model.Loan>?>(null) }
    var deleteText by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var exporting by remember { mutableStateOf(false) }
    val scope=rememberCoroutineScope()
    val latestLoans by rememberUpdatedState(loans)
    val exporter=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if(uri!=null) scope.launch {
            exporting=true
            runCatching { withContext(Dispatchers.IO) {
                requireNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter(Charsets.UTF_8).use { it.write(com.example.data.model.loanCsv(latestLoans)) }
            } }.onSuccess { exportMessage="Loan export saved." }.onFailure { error="Couldn't save the export. Please try again." }
            exporting=false
        }
    }
    val backup=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if(uri!=null) scope.launch {
            exporting=true;error=null
            runCatching { withContext(Dispatchers.IO) { requireNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter().use { it.write(com.example.data.model.LoanBackup.encode(latestLoans)) } } }
                .onSuccess { exportMessage="Backup saved. Keep this file somewhere safe." }.onFailure { error="Could not save backup. Please try again." }
            exporting=false
        }
    }
    val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) scope.launch {
            exporting=true;error=null;exportMessage=null
            runCatching { withContext(Dispatchers.IO) {
                val limit=com.example.data.model.LoanBackup.MAX_BYTES
                val bytes=requireNotNull(context.contentResolver.openInputStream(uri)).use { input ->
                    val result=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
                    while(true) { val count=input.read(buffer);if(count<0)break;require(result.size()+count<=limit) { "Backup must be smaller than 5 MB." };result.write(buffer,0,count) };result.toByteArray()
                }
                com.example.data.model.LoanBackup.decode(bytes.toString(Charsets.UTF_8))
            } }.onSuccess { pendingImport=it }.onFailure { error="Could not read this backup. Select a valid HisabKitab JSON backup smaller than 5 MB." }
            exporting=false
        }
    }
    fun systemPage(action:String,notification:Boolean=false) {
        runCatching { context.startActivity(Intent(action).apply {
            if(notification) putExtra(Settings.EXTRA_APP_PACKAGE,context.packageName)
            else if(action==Settings.ACTION_APPLICATION_DETAILS_SETTINGS) data=Uri.parse("package:${context.packageName}")
        }) }.onFailure { error="Unable to open device settings." }
    }
    BackHandler(onBack=onBack)
    Scaffold(containerColor=LedgerPaper,contentWindowInsets=WindowInsets(0,0,0,0),bottomBar={LedgerNav(Screen.Settings,onNavigate)}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).testTag("settings_detail_page")) {
            LedgerBackBar(onBack,title=title,label="Back to profile")
            HorizontalDivider(color=LedgerInk.copy(alpha=.08f))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                when(title) {
                    "Your Data" -> {
                        SettingInfo("Your ledger","${loans.size} loans  |  ${loans.count { !it.isRepaid }} outstanding  |  ${loans.count { it.isRepaid }} repaid")
                        SettingInfo("Export your loans","Save a CSV file with names, amounts, due dates, repayment status and notes. You can open it in Excel or Google Sheets.")
                        Button(onClick={error=null;exportMessage=null;exporter.launch("HisabKitab-loans.csv")},enabled=!exporting,shape=RoundedCornerShape(10.dp),colors=ButtonDefaults.buttonColors(containerColor=LedgerGreen),modifier=Modifier.fillMaxWidth().height(48.dp)) { Text(if(exporting) "Saving..." else "Export loans as CSV") }
                        exportMessage?.let { Text(it,color=LedgerGreen,fontSize=13.sp) }
                        if(loans.isEmpty()) SettingsAction("Add your first loan") { onNavigate(Screen.AddLoan) }
                        SettingInfo("Backup & restore","A JSON backup can be restored on another phone. CSV is for spreadsheets only. Backup files are not encrypted; keep them private. Profile photos are not included.")
                        Button(onClick={backup.launch("HisabKitab-backup.json")},enabled=!exporting,modifier=Modifier.fillMaxWidth()) { Text("Save backup") }
                        OutlinedButton(onClick={restore.launch(arrayOf("application/json","text/plain","application/octet-stream"))},enabled=!exporting,modifier=Modifier.fillMaxWidth()) { Text("Restore backup") }
                        pendingImport?.let { imported ->
                            SettingInfo("Restore ${imported.size} loans?","Records will be added to this ledger. Exact duplicates are skipped. Your existing loans will remain.")
                            Button(onClick={scope.launch { exporting=true;runCatching { onRestore(imported) }.onSuccess { exportMessage="Restored $it loans.";pendingImport=null }.onFailure { error="Restore failed. Please try again." };exporting=false }},enabled=!exporting,modifier=Modifier.fillMaxWidth()) { Text("Confirm restore") }
                            TextButton(onClick={pendingImport=null}) { Text("Cancel restore") }
                        }
                        SettingInfo("Where your records live","HisabKitab saves your loans on this phone. There is no account, server or automatic cloud backup. Uninstalling or clearing app data removes these records. Save a JSON backup before changing phones.")
                        SettingsAction("Delete all app data") { confirmDelete=true }
                        if(confirmDelete) {
                            SettingInfo("Permanently delete all data?","This removes every loan and the local profile from this installation, including older preview profiles. Saved export files outside the app are not deleted. This cannot be undone.")
                            OutlinedTextField(deleteText,{deleteText=it},label={Text("Type DELETE to confirm")},singleLine=true,modifier=Modifier.fillMaxWidth())
                            Button(onClick={scope.launch { exporting=true;runCatching { onErase() }.onSuccess { deleteText="";confirmDelete=false;pendingImport=null;exportMessage="All app data deleted." }.onFailure { error="Could not delete data. Please try again." };exporting=false }},enabled=deleteText=="DELETE"&&!exporting,colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFB33E32)),modifier=Modifier.fillMaxWidth()) { Text("Delete permanently") }
                            TextButton(onClick={confirmDelete=false;deleteText=""}) { Text("Keep my data") }
                        }
                    }
                    "Personal Information" -> {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            ProfileAvatar(state.profilePhoto,name,Modifier.size(64.dp))
                            TextButton(onClick=onChoosePhoto){Text("Change photo",color=LedgerGreen)}
                            if(state.profilePhoto!=null) TextButton(onClick={onPhotoChange(null)}){Text("Remove",color=LedgerMuted)}
                        }
                        OutlinedTextField(name,{name=it.take(50);saved=false},label={Text("Your name")},singleLine=true,shape=RoundedCornerShape(10.dp),modifier=Modifier.fillMaxWidth().testTag("profile_name_input"))
                        SettingInfo("Local profile","No phone number, password or account is required.")
                        Text("Your name and selected photo stay on this phone. You can use the app without adding either.",fontSize=13.sp,lineHeight=20.sp,color=LedgerMuted)
                        Button(onClick={onSaveName(name.trim());saved=true},shape=RoundedCornerShape(10.dp),colors=ButtonDefaults.buttonColors(containerColor=LedgerGreen),modifier=Modifier.fillMaxWidth().height(48.dp)){Text(if(saved) "Saved" else "Save profile")}
                    }
                    "Privacy & Security" -> {
                        SettingInfo("HisabKitab privacy policy","Effective October 26, 2026. Developer: Paramjit Baral. Contact: aapnastoreofficial@gmail.com.")
                        SettingInfo("Your records","This version keeps your loan records on this device. Keep your phone protected with a screen lock.")
                        SettingInfo("Photo access","You choose which profile image to share through the system photo picker. You can remove it from Personal Information.")
                        SettingsAction("Review app permissions") { systemPage(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) }
                        SettingInfo("Privacy policy","HisabKitab works offline. We do not receive your loans, names, notes or profile photo. There are no ads, analytics, trackers or remote accounts. The app does not request internet, contacts, SMS or location access.")
                        SettingInfo("Files you choose","The system file picker grants access only to a photo or backup you choose. Exports are saved to a location you select. If you select a cloud storage provider, that provider handles the file under its own privacy policy.")
                        SettingInfo("Your choices","Remove your photo in Personal Information. Export, back up, restore or delete records in Your Data. Use a device screen lock; the app does not provide separate database encryption or an app PIN.")
                        SettingInfo("Support messages","If you email the developer, your message and attachments are handled through the email provider to answer your request. Avoid sending sensitive records. You can request deletion of support correspondence by email, subject to applicable legal obligations. This app is not designed specifically for children and collects no data automatically.")
                        SettingInfo("Data retention","Records remain on this device until you delete them, clear app storage or uninstall. External backup/export files remain wherever you saved them. Automatic Android cloud backup is disabled.")
                    }
                    "Help & Support" -> {
                        SettingInfo("Contact the developer","Paramjit Baral · aapnastoreofficial@gmail.com")
                        SettingsAction("Email support") { runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:aapnastoreofficial@gmail.com")).putExtra(Intent.EXTRA_SUBJECT,"HisabKitab support")) }.onFailure { error="No email app found. Contact aapnastoreofficial@gmail.com from your email service." } }
                        SettingInfo("Add a loan","Tap Add loan, enter your friend's name, amount and due date, then save. Add a note to remember what it was for.")
                        SettingInfo("Record a repayment","Open the loan details after receiving the full amount, then choose Mark as repaid. This records a payment; it does not transfer money.")
                        SettingInfo("Loans and History","Loans shows outstanding balances by person. History includes both outstanding and repaid entries.")
                        SettingInfo("Overdue amounts","A loan becomes overdue after its due date if it has not been marked repaid.")
                        SettingsAction("View introduction",onIntroduction)
                    }
                    else -> {
                        Text("HisabKitab",fontSize=28.sp,fontWeight=FontWeight.SemiBold,color=LedgerGreen)
                        Text("A small ledger for money lent to friends.",fontSize=15.sp,lineHeight=22.sp,color=LedgerMuted)
                        SettingInfo("Version",com.example.BuildConfig.VERSION_NAME)
                        SettingInfo("Developer","Paramjit Baral")
                        SettingInfo("Support","aapnastoreofficial@gmail.com")
                        SettingInfo("What you can do","Keep loan details together, track due dates and record repayments. HisabKitab is a personal record keeper, not a lender or payment service. It does not move money, charge interest or send repayment reminders.")
                    }
                }
                error?.let { Text(it,color=MaterialTheme.colorScheme.error,fontSize=13.sp) }
            }
        }
    }
}

@Composable
private fun SettingInfo(title:String,body:String) {
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(title,fontSize=14.sp,fontWeight=FontWeight.Medium,color=LedgerInk)
        Text(body,fontSize=13.sp,lineHeight=20.sp,color=LedgerMuted)
        HorizontalDivider(Modifier.padding(top=12.dp),color=LedgerInk.copy(alpha=.08f))
    }
}
@Composable
private fun SettingsAction(title:String,onClick:()->Unit) {
    OutlinedButton(onClick=onClick,shape=RoundedCornerShape(10.dp),modifier=Modifier.fillMaxWidth().heightIn(min=46.dp)){Text(title,color=LedgerGreen,fontSize=13.sp)}
}
