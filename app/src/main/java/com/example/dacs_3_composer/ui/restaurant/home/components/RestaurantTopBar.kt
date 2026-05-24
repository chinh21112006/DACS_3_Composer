package com.example.dacs_3_composer.ui.restaurant.home.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs_3_composer.R

@Composable
fun RestaurantTopBar(
    name: String,
    avatarUrl: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.banner1),
            error = painterResource(id = R.drawable.banner1)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.ifEmpty { "Restaurant" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2159BC)
            )
            Text("Hệ thống đang vận hành ổn định", fontSize = 12.sp, color = Color(0xFF727785))
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.NotificationsNone, contentDescription = "Thông báo", tint = Color(0xFF2159BC))
        }
    }
}
