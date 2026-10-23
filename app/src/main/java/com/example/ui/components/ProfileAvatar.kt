package com.example.ui.components

import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileAvatar(uri: String?, name: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, uri) {
        value = null
        value = withContext(Dispatchers.IO) {
            runCatching {
                uri?.let {
                    if(android.os.Build.VERSION.SDK_INT < 28) {
                        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        context.contentResolver.openInputStream(Uri.parse(it))?.use { stream -> android.graphics.BitmapFactory.decodeStream(stream, null, bounds) }
                        val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = (maxOf(bounds.outWidth, bounds.outHeight) / 320).coerceAtLeast(1) }
                        context.contentResolver.openInputStream(Uri.parse(it))?.use { stream -> android.graphics.BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap() }
                    } else ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, Uri.parse(it))) { decoder, info, _ ->
                        val scale = 320f / maxOf(info.size.width, info.size.height)
                        if(scale < 1f) decoder.setTargetSize((info.size.width * scale).toInt().coerceAtLeast(1), (info.size.height * scale).toInt().coerceAtLeast(1))
                    }.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    Box(modifier.clip(CircleShape).background(LedgerPaper), contentAlignment = Alignment.Center) {
        bitmap?.let { Image(it, "Profile photo", Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            ?: if (!name.isNullOrBlank()) Text(name.take(1).uppercase(), color = LedgerGreen, fontSize = 22.sp)
            else Icon(Icons.Outlined.PersonOutline, "Profile", tint = LedgerGreen, modifier = Modifier.fillMaxSize(.48f))
    }
}
