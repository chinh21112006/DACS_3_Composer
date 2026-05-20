package com.example.dacs_3_composer.ui.admin.shipper.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShipperPagination() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút lùi trang
        IconButton(
            onClick = {},
            modifier = Modifier.size(32.dp).border(1.dp, Color(0xFFE0E0E0), CircleShape)
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trang 1 (Đang chọn)
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFF0052CC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("1", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trang 2
        Box(
            modifier = Modifier.size(32.dp).border(1.dp, Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("2", color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trang 3
        Box(
            modifier = Modifier.size(32.dp).border(1.dp, Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("3", color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Nút tiến trang
        IconButton(
            onClick = {},
            modifier = Modifier.size(32.dp).border(1.dp, Color(0xFFE0E0E0), CircleShape)
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}