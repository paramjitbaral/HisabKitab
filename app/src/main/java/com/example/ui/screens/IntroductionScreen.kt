package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.core.view.WindowCompat
import com.example.R
import com.example.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun IntroductionScreen(onFinish: () -> Unit) {
    val pager = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    SideEffect { WindowCompat.getInsetsController((view.context as android.app.Activity).window, view).isAppearanceLightStatusBars = pager.currentPage != 1 }
    Box(Modifier.fillMaxSize().background(LedgerPaper).testTag("introduction_screen")) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val compact = maxHeight < 740.dp
                val background = when(page) { 0 -> R.drawable.intro_ledger; 1 -> R.drawable.intro_history; else -> R.drawable.intro_private }
                Image(painterResource(background), null, if(page == 1) Modifier.fillMaxWidth().fillMaxHeight(.79f) else Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                if(page == 0) {
                    Column(Modifier.statusBarsPadding().padding(horizontal=28.dp).padding(top=26.dp)) {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoStories, null, tint=LedgerGreen, modifier=Modifier.size(28.dp))
                            Spacer(Modifier.width(9.dp)); LedgerTitle("MiniLedger", size=21)
                        }
                        Spacer(Modifier.height(if(compact) 30.dp else 42.dp))
                        LedgerTitle("Your\nLoans.", size=if(compact) 42 else 47)
                        LedgerTitle("In One Place.", color=LedgerGreen, size=if(compact) 36 else 40)
                        Spacer(Modifier.height(12.dp))
                        Text("Track, manage and keep\nall your loans organized.", color=Color(0xFF59615E), fontSize=15.sp,lineHeight=21.sp)
                        Spacer(Modifier.height(20.dp)); Box(Modifier.size(34.dp,1.dp).background(LedgerInk)); Spacer(Modifier.height(20.dp))
                        IntroFeature(Icons.Outlined.FormatListBulleted,"Track all loans","See who owes what",false)
                        IntroFeature(Icons.Outlined.CalendarMonth,"Know your due dates","Keep repayments in view",false)
                        IntroFeature(Icons.Outlined.BarChart,"Stay organized","Simple and easy to use",false)
                    }
                }
                if(page == 2) {
                    Column(Modifier.statusBarsPadding().padding(horizontal=28.dp).padding(top=78.dp)) {
                        Box(Modifier.size(48.dp).background(LedgerPaper, RoundedCornerShape(14.dp)), contentAlignment=Alignment.Center) { Icon(Icons.Outlined.Lock,null,tint=LedgerGreen,modifier=Modifier.size(28.dp)) }
                        Spacer(Modifier.height(22.dp))
                        LedgerTitle("Simple.\nSecure. Yours.",size=if(compact) 36 else 40)
                        Spacer(Modifier.height(14.dp))
                        Text("Your own space to keep\ntrack of money you lend.",fontSize=15.sp,lineHeight=22.sp,color=Color(0xFF59615E))
                        Spacer(Modifier.height(if(compact) 26.dp else 36.dp))
                        IntroFeature(Icons.Outlined.Shield,"Your personal ledger","Linked to your account",true)
                        IntroFeature(Icons.Outlined.PhoneAndroid,"Close at hand","Saved on this device",true)
                        IntroFeature(Icons.Outlined.PersonOutline,"Built for your control","Simple, transparent\nand reliable",true)
                    }
                }
            }
        }
        TextButton(onClick=onFinish, modifier=Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top=18.dp,end=18.dp).testTag("intro_skip")) {
            Text("Skip",color=if(pager.currentPage==0) LedgerInk else Color.White,fontSize=15.sp)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .clip(when(pager.currentPage){0->IntroRise;1->IntroWave;else->IntroValley}).background(LedgerPaper)
            .navigationBarsPadding().padding(horizontal=28.dp).padding(top=if(pager.currentPage==1) 56.dp else 26.dp,bottom=22.dp)) {
            if(pager.currentPage==1) {
                LedgerTitle("Stay on top\nevery day.",size=32)
                Spacer(Modifier.height(10.dp))
                Text("See due dates and keep\nyour loans organized effortlessly.",fontSize=14.sp,lineHeight=20.sp,color=Color(0xFF59615E))
                Spacer(Modifier.height(14.dp))
            }
            Row(Modifier.fillMaxWidth().padding(bottom=22.dp),horizontalArrangement=if(pager.currentPage==0) Arrangement.Start else Arrangement.Center) {
                repeat(3) { index -> Box(Modifier.padding(end=10.dp).size(9.dp).background(if(index==pager.currentPage) LedgerGreen else Color(0xFFD2CEC5),CircleShape)) }
            }
            Button(onClick={if(pager.currentPage==2) onFinish() else scope.launch { pager.animateScrollToPage(pager.currentPage+1) }},
                modifier=Modifier.fillMaxWidth().height(52.dp).testTag("intro_next"),shape=RoundedCornerShape(28.dp),colors=ButtonDefaults.buttonColors(containerColor=LedgerGreen)) {
                Text(if(pager.currentPage==2) "Get Started" else "Next",fontSize=16.sp)
                Spacer(Modifier.width(12.dp)); Icon(Icons.Outlined.ArrowForward,null,Modifier.size(21.dp))
            }
        }
    }
}

@Composable
private fun IntroFeature(icon: ImageVector, title: String, subtitle: String, dark: Boolean) {
    Row(Modifier.padding(bottom=18.dp),verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(if(dark) 46.dp else 38.dp).background(if(dark) LedgerGreen else Color(0xFFE2E1D5),CircleShape),contentAlignment=Alignment.Center) {
            Icon(icon,null,tint=if(dark) Color.White else LedgerGreen,modifier=Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column { Text(title,fontSize=13.sp,lineHeight=18.sp,color=LedgerInk);Text(subtitle,fontSize=11.sp,lineHeight=17.sp,color=Color(0xFF59615E)) }
    }
}

private val IntroWave = GenericShape { size, _ ->
    moveTo(0f,0f)
    cubicTo(size.width*.35f, size.height*.04f, size.width*.65f, size.height*.24f, size.width, size.height*.13f)
    lineTo(size.width,size.height);lineTo(0f,size.height);close()
}

private val IntroRise = GenericShape { size, _ ->
    moveTo(0f,size.height*.20f)
    cubicTo(size.width*.25f,size.height*.02f,size.width*.65f,size.height*.03f,size.width,0f)
    lineTo(size.width,size.height);lineTo(0f,size.height);close()
}
private val IntroValley = GenericShape { size, _ ->
    moveTo(0f,size.height*.12f)
    cubicTo(size.width*.25f,-size.height*.04f,size.width*.6f,size.height*.30f,size.width,0f)
    lineTo(size.width,size.height);lineTo(0f,size.height);close()
}
