package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.ui.Screen
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

val LedgerPaper = Color.White
val LedgerGreen = Color(0xFF073D2E)
val LedgerInk = Color(0xFF10272A)
val LedgerMuted = Color(0xFF7D8385)
val LedgerGradient = Brush.linearGradient(listOf(Color(0xFF123E32), Color(0xFF082D24)))
fun money(value: Double): String = "₹ " + NumberFormat.getNumberInstance(Locale("en", "IN")).format(value)

@Composable
fun LedgerTitle(text: String, modifier: Modifier = Modifier, color: Color = LedgerInk, size: Int = 26) {
    Text(text, modifier, color = color, fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold, fontSize = size.sp, lineHeight = (size + 6).sp)
}

@Composable
fun LedgerBackBar(onBack:()->Unit,modifier:Modifier=Modifier,title:String?=null,label:String="Back",titleColor:Color=LedgerInk,trailing:@Composable RowScope.()->Unit={}) {
    Row(modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=20.dp).padding(top=8.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically) {
        RoundAction(Icons.AutoMirrored.Outlined.ArrowBack,label,onBack)
        if(title!=null) Text(title,Modifier.weight(1f).padding(start=12.dp),fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=titleColor)
        else Spacer(Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun LedgerPhotoHeading(title:String,subtitle:String,onBack:()->Unit,modifier:Modifier=Modifier,trailing:@Composable RowScope.()->Unit={}) {
    Column(modifier.fillMaxWidth()) {
        LedgerBackBar(onBack,trailing=trailing)
        Column(Modifier.padding(horizontal=20.dp).padding(top=12.dp,bottom=40.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            LedgerTitle(title,color=Color.White,size=28)
            Text(subtitle,color=Color.White,fontSize=13.sp,lineHeight=20.sp)
        }
    }
}

@Composable
fun RoundAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick, modifier.size(44.dp)) {
        Box(Modifier.size(34.dp).background(Color.White,CircleShape),contentAlignment=Alignment.Center) {
            Icon(icon, label, tint = LedgerInk, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun LedgerNav(screen: Screen, onNavigate: (Screen) -> Unit) {
    Box(Modifier.fillMaxWidth().background(LedgerGreen).navigationBarsPadding()) {
    Surface(color=LedgerGreen,tonalElevation=0.dp) {
        Column {
        HorizontalDivider(color=Color.White.copy(alpha=.1f),thickness=1.dp)
        Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal=8.dp),
            horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically) {
            NavEnd("Add loan",Icons.Outlined.Add,screen==Screen.AddLoan){onNavigate(Screen.AddLoan)}
            NavEnd("Loans",Icons.Outlined.Description,screen==Screen.Loans){onNavigate(Screen.Loans)}
            NavEnd("Home",Icons.Outlined.Home,screen==Screen.Dashboard){onNavigate(Screen.Dashboard)}
            NavEnd("History",Icons.Outlined.History,screen==Screen.History){onNavigate(Screen.History)}
            NavEnd("Profile",Icons.Outlined.PersonOutline,screen==Screen.Settings){onNavigate(Screen.Settings)}
        }
        }
    }
    }
}

@Composable
private fun NavEnd(label:String,icon:ImageVector,selected:Boolean,onClick:()->Unit) {
    Surface(onClick=onClick,shape=RoundedCornerShape(12.dp),color=Color.Transparent) {
        Column(Modifier.width(58.dp).height(56.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            Box(Modifier.size(38.dp,26.dp).background(if(selected) Color.White.copy(alpha=.12f) else Color.Transparent,RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center) {
                Icon(icon,null,tint=if(label=="Home") Color(0xFFE6C572) else Color.White,modifier=Modifier.size(21.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(label,color=if(label=="Home") Color(0xFFE6C572) else Color.White,fontSize=9.sp,lineHeight=12.sp)

        }
    }
}

@Composable
fun LedgerSaveButton(text: String, loading: Boolean = false, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, enabled = !loading, modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B382B))) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        else { Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium); Spacer(Modifier.width(8.dp)); Icon(Icons.Outlined.ArrowForward, null, Modifier.size(19.dp)) }
    }
}
