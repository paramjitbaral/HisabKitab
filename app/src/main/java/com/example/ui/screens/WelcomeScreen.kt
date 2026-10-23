package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.R
import com.example.ui.components.*

@Composable
fun WelcomeScreen() {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.welcome_desk),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Column(Modifier.fillMaxWidth().padding(top=maxHeight*.26f).padding(horizontal=32.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Box(Modifier.size(88.dp).background(LedgerGreen,RoundedCornerShape(23.dp)),contentAlignment=Alignment.Center) {
                Icon(Icons.Outlined.AutoStories,null,tint=Color(0xFFFFF8EF),modifier=Modifier.size(48.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("Welcome to\nMiniLedger",fontFamily=androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(R.font.dm_serif_display)),fontSize=31.sp,lineHeight=34.sp,color=LedgerInk,textAlign=TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Text("A simple, trusted way to manage\nyour loans and relationships.",fontSize=13.sp,lineHeight=20.sp,color=LedgerInk,textAlign=TextAlign.Center)
        }
    }
}
