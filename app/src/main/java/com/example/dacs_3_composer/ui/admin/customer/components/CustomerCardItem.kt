package com.example.dacs_3_composer.ui.admin.customer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // 🎯 Đảm bảo bạn đã thêm dependency Coil vào build.gradle nhé

@Composable
fun CustomerCardItem(
    name: String,
    phone: String,
    email: String,      // 🎯 Thay thế cho address cũ
    role: String,       // 🎯 Thêm mới thuộc tính Vai trò
    avatarUrl: String,  // 🎯 Thêm mới thuộc tính Ảnh đại diện
    isAvailable: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLockToggle: () -> Unit
) {
    val isLocked = !isAvailable

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 🎯 THAY ĐỔI: Hiển thị Avatar từ URL (nếu trống thì fallback về dạng Box chữ cái)
                if (avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar $name",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF191C1D))
                    Text(text = phone, fontSize = 13.sp, color = Color(0xFF727785))
                }

                // Badge Trạng thái hoạt động
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isLocked) Color(0xFFFFEBEE) else Color(0xFFE8F8F0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isLocked) "ĐÃ KHÓA" else "ĐANG HOẠT ĐỘNG",
                        fontSize = 10.sp,
                        color = if (isLocked) Color(0xFFE57373) else Color(0xFF2ECC71),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🎯 THAY ĐỔI: Thêm thông tin Role phía trên Email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Vai trò: ", fontSize = 13.sp, color = Color(0xFF727785))
                Text(
                    text = role.ifBlank { "USER" }.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0052CC)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 🎯 THAY ĐỔI: Chuyển đổi nhãn hiển thị từ "Địa chỉ" thành "Email"
            Text(
                text = "Email: ${email.ifBlank { "Chưa cập nhật email" }}",
                fontSize = 13.sp,
                color = Color(0xFF4A5568)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hàng nút hành động: Sửa, Xóa, Khóa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Sửa", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Xóa", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                IconButton(
                    onClick = onLockToggle,
                    modifier = Modifier
                        .background(color = if (isLocked) Color(0xFFFFEAEA) else Color(0xFFF5F6F8), shape = CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Outlined.LockOpen else Icons.Outlined.Block,
                        contentDescription = null,
                        tint = if (isLocked) Color(0xFFE74C3C) else Color(0xFF727785),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}