package com.example.dacs_3_composer.ui.shipper.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShipperDashboardTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar tròn của tài xế
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6)) // Màu nền đại diện avatar tài xế
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chào buổi sáng,",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Minh Trần",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D4ED8)
            )
        }

        IconButton(onClick = {}) {
            BadgedBox(
                badge = { Badge(containerColor = Color(0xFFEF4444), modifier = Modifier.size(8.dp)) }
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}