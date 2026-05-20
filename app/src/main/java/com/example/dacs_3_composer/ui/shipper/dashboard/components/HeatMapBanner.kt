package com.example.dacs_3_composer.ui.shipper.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeatMapBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A)), // Màu tối giả lập bản đồ đêm công nghệ cao
        contentAlignment = Alignment.Center
    ) {
        // Bạn có thể thêm một bức ảnh map chìm ở đây nếu cần thiết, tạm thời dùng màu nền
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mở bản đồ nhiệt",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
        }
    }
}