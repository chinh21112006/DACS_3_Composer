package com.example.dacs_3_composer.ui.admin.shipper.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShipperStatsGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(label = "Tổng shipper", value = "124", valueColor = Color.Black, modifier = Modifier.weight(1f))
            MiniStatCard(label = "Đang online", value = "86", valueColor = Color(0xFF2ECC71), modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(label = "Đơn chờ lấy", value = "12", valueColor = Color(0xFFE74C3C), modifier = Modifier.weight(1f))
            MiniStatCard(label = "Hiệu suất tb", value = "98%", valueColor = Color.Black, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, fontSize = 12.sp, color = Color(0xFF727785), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}