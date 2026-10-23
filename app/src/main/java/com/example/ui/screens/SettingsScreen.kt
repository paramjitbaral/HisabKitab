package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import com.example.R
import com.example.ui.MiniLedgerUiState
import com.example.ui.components.*

@Composable
fun SettingsScreen(state: MiniLedgerUiState, onBack: () -> Unit, onSaveName: (String) -> Unit,
    onPhotoChange: (String?) -> Unit, onLogout: () -> Unit, onIntroduction: () -> Unit = {}, onNavigate: (com.example.ui.Screen) -> Unit = {}) {
    val context = LocalContext.current
    val actualName = state.currentUser?.name?.takeIf { it != "User" && it.any(Char::isLetter) }.orEmpty()


    var error by remember { mutableStateOf<String?>(null) }
    var section by rememberSaveable { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri != null) runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            onPhotoChange(uri.toString())
        }.onFailure { error = "Couldn't open that photo. Please choose another." }
    }
    if(section != null) {
        SettingsDetailPage(section!!,state,{section=null},onSaveName,{picker.launch(arrayOf("image/*"))},onPhotoChange,onLogout,onIntroduction,onNavigate)
        return
    }
    Scaffold(containerColor=LedgerPaper,contentWindowInsets=WindowInsets(0,0,0,0), bottomBar={LedgerNav(com.example.ui.Screen.Settings,onNavigate)}) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).background(LedgerPaper).verticalScroll(rememberScrollState()).testTag("settings_screen")) {
        Box(Modifier.fillMaxWidth().height(220.dp).zIndex(1f)) {
            Image(painterResource(R.drawable.onboarding_ledger),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.16f)))
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=16.dp,vertical=12.dp), verticalAlignment = Alignment.CenterVertically) {
                RoundAction(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onBack)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Outlined.MenuBook,null,tint=Color.White,modifier=Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                LedgerTitle("MiniLedger",color=Color.White,size=23)
            }
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(30.dp).background(LedgerPaper,RoundedCornerShape(topStart=60.dp,topEnd=20.dp)))
            Box(Modifier.align(Alignment.BottomStart).padding(start=24.dp).offset(y=24.dp).size(90.dp).clickable { picker.launch(arrayOf("image/*")) }.testTag("profile_photo_picker")) {
                ProfileAvatar(state.profilePhoto,actualName,Modifier.fillMaxSize().border(2.dp,LedgerPaper,androidx.compose.foundation.shape.CircleShape))
                Icon(Icons.Outlined.CameraAlt,"Change profile photo",tint=Color.White,modifier=Modifier.align(Alignment.BottomEnd).background(LedgerGreen,androidx.compose.foundation.shape.CircleShape).padding(7.dp).size(16.dp))
            }
        }
        Surface(Modifier.fillMaxWidth(), color = LedgerPaper) {
            Column(Modifier.padding(horizontal=22.dp).padding(top=34.dp,bottom=22.dp), horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                Text(actualName.ifBlank { "Your profile" },color=LedgerInk,fontSize=22.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold)
                if(!state.currentUser?.phoneNumber.isNullOrBlank()) Text(state.currentUser?.phoneNumber.orEmpty(), color = LedgerMuted, fontSize = 12.sp)
                }
                OutlinedButton(onClick={section="Personal Information"},shape=RoundedCornerShape(9.dp),contentPadding=PaddingValues(horizontal=10.dp,vertical=0.dp)) { Icon(Icons.Outlined.Edit,null,Modifier.size(15.dp));Spacer(Modifier.width(5.dp));Text("Edit Profile",fontSize=11.sp,color=LedgerGreen) }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                Spacer(Modifier.height(16.dp))
                listOf(Icons.Outlined.Person to "Personal Information",Icons.Outlined.Notifications to "Notifications",Icons.Outlined.Shield to "Privacy & Security",Icons.Outlined.Palette to "App Preferences",Icons.Outlined.HelpOutline to "Help & Support",Icons.Outlined.Info to "About MiniLedger").forEach { (icon,label) ->
                    HorizontalDivider(color=LedgerInk.copy(alpha=.08f))
                    Row(Modifier.fillMaxWidth().clickable { section=label }.padding(vertical=14.dp),verticalAlignment=Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).background(Color(0xFFDFE4D7),androidx.compose.foundation.shape.CircleShape),contentAlignment=Alignment.Center) { Icon(icon,null,tint=LedgerGreen,modifier=Modifier.size(22.dp)) }
                        Spacer(Modifier.width(14.dp));Text(label,color=LedgerInk,fontSize=14.sp,modifier=Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight,null,tint=LedgerMuted,modifier=Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = LedgerInk.copy(alpha = .1f))
                TextButton(onClick = onLogout, shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.textButtonColors(containerColor=Color(0xFFF5E4DC)),modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(50.dp).testTag("confirm_logout_button")) {
                    Icon(Icons.Outlined.Logout, null, Modifier.size(18.dp), tint = Color(0xFFAA463A))
                    Spacer(Modifier.width(8.dp)); Text("Log out", color = Color(0xFFAA463A))
                }
                Text("MiniLedger", color = LedgerMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 24.dp))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
    }
}
