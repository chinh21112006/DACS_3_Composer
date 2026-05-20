package com.example.dacs_3_composer.ui.admin.overview.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewTopBar(
    adminName: String = "Gourmet Admin",
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner1), // Thay bằng avatar mặc định của bạn
            contentDescription = "Admin Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chào buổi sáng,",
                fontSize = 12.sp,
                color = Color(0xFF727785)
            )
            Text(
                text = adminName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2159BC)
            )
        }
        IconButton(onClick = onNotificationClick) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = Color.Red,
                        modifier = Modifier.size(8.dp)
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Thông báo",
                    tint = Color(0xFF2159BC),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}