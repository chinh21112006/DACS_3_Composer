package com.example.dacs_3_composer.ui.admin.customer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerTopBar(
    onAddUserClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp) // Tinh chỉnh khoảng cách vừa vặn
    ) {
        // Tiêu đề chính màn hình
        Text(
            text = "Quản lý Khách hàng",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191C1D)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Dòng mô tả chức năng ngắn gọn
        Text(
            text = "Quản lý và theo dõi hành vi mua sắm của khách hàng.",
            fontSize = 14.sp,
            color = Color(0xFF727785)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Nút bấm thực hiện thêm tài khoản mới lên hệ thống
        Button(
            onClick = onAddUserClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAddAlt1,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Thêm Khách Hàng Mới",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}