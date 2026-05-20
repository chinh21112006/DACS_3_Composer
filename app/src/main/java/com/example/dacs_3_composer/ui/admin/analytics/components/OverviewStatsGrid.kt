package com.example.dacs_3_composer.ui.admin.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverviewStatsGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsStatCard(
                title = "Tổng doanh thu", value = "1.28Bđ", trend = "↗ +12.5%", isPositive = true,
                icon = Icons.Default.InsertChart, iconBg = Color(0xFFE8F0FE), iconColor = Color(0xFF0052CC),
                modifier = Modifier.weight(1f)
            )
            AnalyticsStatCard(
                title = "Đơn hàng mới", value = "12,450", trend = "↗ +8.2%", isPositive = true,
                icon = Icons.Default.Assignment, iconBg = Color(0xFFFCE8E6), iconColor = Color(0xFFA94442),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsStatCard(
                title = "Người dùng mới", value = "3,120", trend = "↘ -2.4%", isPositive = false,
                icon = Icons.Default.Group, iconBg = Color(0xFFF1F3F4), iconColor = Color(0xFF5F6368),
                modifier = Modifier.weight(1f)
            )
            AnalyticsStatCard(
                title = "Đánh giá TB", value = "4.8/5", trend = "↗ +0.3", isPositive = true,
                icon = Icons.Default.Star, iconBg = Color(0xFFFEF7E0), iconColor = Color(0xFFF1C40F),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    title: String,
    value: String,
    trend: String,
    isPositive: Boolean,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(36.dp).background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 12.sp, color = Color(0xFF727785))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = trend,
                fontSize = 12.sp,
                color = if (isPositive) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                fontWeight = FontWeight.Medium
            )
        }
    }
}