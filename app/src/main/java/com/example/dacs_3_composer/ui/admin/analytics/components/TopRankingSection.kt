package com.example.dacs_3_composer.ui.admin.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopRankingSection() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Nhà hàng bán chạy", "Món ăn phổ biến", "Shipper tích cực")

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Hàng chuyển Tab
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    label = { Text(title) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0052CC),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFE8EFFF),
                        labelColor = Color(0xFF0052CC)
                    ),
                    border = null
                )
            }
        }

        // Danh sách các Item trong Top xếp hạng
        RankingRowItem(rank = 1, rankBg = Color(0xFFF1C40F), name = "The Gourmet Bistro", desc = "Quận 1, TP. Hồ Chí Minh", revenue = "450.2Mđ", orders = "1,240 đơn hàng")
        RankingRowItem(rank = 2, rankBg = Color(0xFFBDC3C7), name = "Pizza Italia", desc = "Quận 7, TP. Hồ Chí Minh", revenue = "312.8Mđ", orders = "980 đơn hàng")
        RankingRowItem(rank = 3, rankBg = Color(0xFFE67E22), name = "Sakura Sushi", desc = "Quận Hai Bà Trưng, Hà Nội", revenue = "285.5Mđ", orders = "750 đơn hàng")
    }
}

@Composable
private fun RankingRowItem(
    rank: Int,
    rankBg: Color,
    name: String,
    desc: String,
    revenue: String,
    orders: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Badge xếp hạng đè nhẹ hoặc đứng trước Avatar hình ảnh
            Box(modifier = Modifier.size(54.dp)) {
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
                Box(
                    modifier = Modifier.size(18.dp).background(rankBg, CircleShape).align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = rank.toString(), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin mô tả chính
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Text(text = desc, fontSize = 12.sp, color = Color.Gray)
            }

            // Số liệu doanh số
            Column(horizontalAlignment = Alignment.End) {
                Text(text = revenue, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0052CC))
                Text(text = orders, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}