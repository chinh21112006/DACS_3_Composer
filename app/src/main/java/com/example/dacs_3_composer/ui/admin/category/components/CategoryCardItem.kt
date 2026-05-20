package com.example.dacs_3_composer.ui.admin.category.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.MoreVert
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
fun CategoryCardItem(
    title: String,
    description: String,
    itemCount: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Hàng đầu tiên: Biểu tượng & Nút tùy chọn mở rộng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nội dung Tên và Mô tả danh mục
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1D))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 13.sp, color = Color(0xFF727785))

            Spacer(modifier = Modifier.height(14.dp))

            // Hàng dưới cùng: Badge số lượng món & Link "Chi tiết"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEEF2FF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = itemCount, fontSize = 12.sp, color = Color(0xFF0052CC), fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.clickable { },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Chi tiết", fontSize = 13.sp, color = Color(0xFF0052CC), fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF0052CC), modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}