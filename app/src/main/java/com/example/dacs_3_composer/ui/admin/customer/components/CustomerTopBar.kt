package com.example.dacs_3_composer.ui.admin.customer.components

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTopBar(
    adminName: String = "Gourmet Admin"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Hàng Header Admin
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = adminName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1D),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = Color(0xFF191C1D)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tiêu đề chức năng
        Text(
            text = "Quản lý Khách hàng",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191C1D)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Quản lý và theo dõi hành vi mua sắm của khách hàng.",
            fontSize = 14.sp,
            color = Color(0xFF727785)
        )
    }
}