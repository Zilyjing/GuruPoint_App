package com.example.gurupoint.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.ui.theme.DangerRed
import com.example.gurupoint.ui.theme.SuccessGreen

@Composable
fun GameStatusBadge(isOpen: Boolean) {
    val bgColor = if (isOpen) SuccessGreen.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f)
    val textColor = if (isOpen) SuccessGreen else DangerRed
    val text = if (isOpen) "OPEN" else "CLOSED"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WhatsAppSupportButton(
    phoneNumber: String = "919999999999",
    message: String = "Hello, I need help with GuruPoint app."
) {
    val context = LocalContext.current
    Button(
        onClick = { openWhatsApp(context, phoneNumber, message) },
        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.SupportAgent,
            contentDescription = "WhatsApp Support",
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "WhatsApp Support", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

fun openWhatsApp(context: Context, number: String, message: String) {
    try {
        val url = "https://api.whatsapp.com/send?phone=$number&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback
    }
}
