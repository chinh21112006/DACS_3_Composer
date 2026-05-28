package com.example.dacs_3_composer.ui.admin.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs_3_composer.R

@Composable
fun AnalyticsTopBar(
    name: String = "Admin",
    avatarUrl: String = "",
    onChatClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Hàng Avatar, Tên và Các icon chức năng
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl.ifEmpty { R.drawable.banner1 })
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() },
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.banner1),
                error = painterResource(id = R.drawable.banner1)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name.ifEmpty { "Gourmet Admin" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1D),
                modifier = Modifier.weight(1f).clickable { onAvatarClick() }
            )
            IconButton(onClick = onChatClick) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Tin nhắn", tint = Color(0xFF191C1D))
            }
            IconButton(onClick = {}) {
                BadgedBox(badge = { Badge(containerColor = Color.Red) }) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = "Thông báo", tint = Color(0xFF191C1D))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tiêu đề báo cáo
        Text(text = "Báo cáo & Thống kê", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1D))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Dữ liệu được cập nhật mới nhất hôm nay", fontSize = 13.sp, color = Color(0xFF727785))
    }
}