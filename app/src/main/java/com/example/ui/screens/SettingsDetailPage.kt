package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
fun SettingsDetailPage(title:String,state:MiniLedgerUiState,onBack:()->Unit,onSaveName:(String)->Unit,onChoosePhoto:()->Unit,onPhotoChange:(String?)->Unit,onLogout:()->Unit,onIntroduction:()->Unit,onNavigate:(Screen)->Unit) {
    val context=LocalContext.current
    DisposableEffect(Unit) {
        val window=(context as? android.app.Activity)?.window
        val controller=window?.let { androidx.core.view.WindowCompat.getInsetsController(it,it.decorView) }
        val previous=controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars=true
        onDispose { if(previous!=null) controller.isAppearanceLightStatusBars=previous }
    }
    var name by rememberSaveable { mutableStateOf(state.currentUser?.name?.takeUnless { it=="User" }.orEmpty()) }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    fun systemPage(action:String,notification:Boolean=false) {
        runCatching { context.startActivity(Intent(action).apply {
            if(notification) putExtra(Settings.EXTRA_APP_PACKAGE,context.packageName)
            else if(action==Settings.ACTION_APPLICATION_DETAILS_SETTINGS) data=Uri.parse("package:${context.packageName}")
        }) }.onFailure { error="Unable to open device settings." }
    }
    BackHandler(onBack=onBack)
    Scaffold(containerColor=LedgerPaper,contentWindowInsets=WindowInsets(0,0,0,0),bottomBar={LedgerNav(Screen.Settings,onNavigate)}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).statusBarsPadding().testTag("settings_detail_page")) {
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick=onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back to profile",tint=LedgerInk,modifier=Modifier.size(22.dp)) }
                Text(title,fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=LedgerInk)
            }
            HorizontalDivider(color=LedgerInk.copy(alpha=.08f))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                when(title) {
                    "Personal Information" -> {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            ProfileAvatar(state.profilePhoto,name,Modifier.size(64.dp))
                            TextButton(onClick=onChoosePhoto){Text("Change photo",color=LedgerGreen)}
                            if(state.profilePhoto!=null) TextButton(onClick={onPhotoChange(null)}){Text("Remove",color=LedgerMuted)}
                        }
                        OutlinedTextField(name,{name=it.take(50);saved=false},label={Text("Your name")},singleLine=true,shape=RoundedCornerShape(10.dp),modifier=Modifier.fillMaxWidth().testTag("profile_name_input"))
                        SettingInfo("Phone number",state.currentUser?.phoneNumber.orEmpty().ifBlank { "Not available" })
                        Text("Your phone number identifies your account. To use another number, sign out and sign in again.",fontSize=13.sp,lineHeight=20.sp,color=LedgerMuted)
                        Button(onClick={onSaveName(name.trim());saved=true},shape=RoundedCornerShape(10.dp),colors=ButtonDefaults.buttonColors(containerColor=LedgerGreen),modifier=Modifier.fillMaxWidth().height(48.dp)){Text(if(saved) "Saved" else "Save profile")}
                    }
                    "Notifications" -> {
                        SettingInfo("Repayment reminders","Check upcoming due dates on Home and overdue balances in Loans.")
                        SettingInfo("Push notifications","Scheduled notifications are not available yet. Your loan status stays visible in the app.")
                        SettingsAction("Open notification settings") { systemPage(Settings.ACTION_APP_NOTIFICATION_SETTINGS,true) }
                        Text("Device notification permissions do not schedule reminders by themselves.",fontSize=12.sp,lineHeight=18.sp,color=LedgerMuted)
                    }
                    "Privacy & Security" -> {
                        SettingInfo("Your records","This version keeps your loan records on this device. Keep your phone protected with a screen lock.")
                        SettingInfo("Photo access","You choose which profile image to share through the system photo picker. You can remove it from Personal Information.")
                        SettingsAction("Review app permissions") { systemPage(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) }
                        SettingsAction("Sign out of this account",onLogout)
                    }
                    "App Preferences" -> {
                        SettingInfo("Currency","Indian rupee · INR (₹)")
                        SettingInfo("Appearance","Warm cream & forest green")
                        SettingInfo("Text size","MiniLedger follows your device’s font-size setting.")
                        SettingsAction("Open display settings") { systemPage(Settings.ACTION_DISPLAY_SETTINGS) }
                    }
                    "Help & Support" -> {
                        SettingInfo("Add a loan","Tap Add loan, enter your friend’s name, amount and due date, then save. Add a note to remember what it was for.")
                        SettingInfo("Record a repayment","Open the loan details after receiving the full amount, then choose Mark as repaid. This records a payment; it does not transfer money.")
                        SettingInfo("Loans and History","Loans shows outstanding balances by person. History includes both outstanding and repaid entries.")
                        SettingInfo("Overdue amounts","A loan becomes overdue after its due date if it has not been marked repaid.")
                        SettingsAction("View introduction",onIntroduction)
                    }
                    else -> {
                        Text("MiniLedger",fontSize=28.sp,fontWeight=FontWeight.SemiBold,color=LedgerGreen)
                        Text("A small ledger for money lent to friends.",fontSize=15.sp,lineHeight=22.sp,color=LedgerMuted)
                        SettingInfo("Version","1.0 · Android preview")
                        SettingInfo("What you can do","Keep loan details together, track due dates and record repayments.")
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
