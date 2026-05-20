package com.example.dacs_3_composer.ui.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.ui.shipper.profile.components.DriverStatsCard
import com.example.dacs_3_composer.ui.shipper.profile.components.ProfileMenuItem

@Composable
fun ShipperProfileScreen(
    onLogoutClick: () -> Unit,
    onNavigateToSection: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Gourmet Admin", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                IconButton(onClick = { /* Xử lý thông báo */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF2563EB))
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🌟 KHỐI AVATAR (Không cần ảnh nền bìa)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                // Vòng tròn avatar lớn
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_my_calendar), // Dùng drawable icon tạm thời hoặc AsyncImage
                        contentDescription = "Driver Avatar",
                        modifier = Modifier.fillMaxSize(0.6f),
                        tint = Color.Gray
                    )
                }
                // Nút sửa thông tin nhỏ (Bút chì màu xanh dương)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { onNavigateToSection("EDIT_PROFILE") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tên và thông tin xe cộ của Shipper
            Text(text = "Minh Trần", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                Text(text = " 4.9  •  Honda SH 150i", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thẻ chứa 3 khối thống kê (Tổng đơn, tỷ lệ, ngày tham gia)
            DriverStatsCard(
                totalOrders = 1284,
                successRate = 98,
                joinDate = "12/2022"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tiêu đề phân khu cài đặt
            Text(
                text = "CÀI ĐẶT TÀI KHOẢN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Danh sách các Menu điều hướng cài đặt
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    iconContainerColor = Color(0xFFEEF2FF),
                    iconTint = Color(0xFF4F46E5),
                    title = "Account Info",
                    subtitle = "Thông tin cá nhân & Liên hệ",
                    onClick = { onNavigateToSection("ACCOUNT_INFO") }
                )

                ProfileMenuItem(
                    icon = Icons.Default.DirectionsBike,
                    iconContainerColor = Color(0xFFF1F5F9),
                    iconTint = Color(0xFF475569),
                    title = "Vehicle Management",
                    subtitle = "Quản lý phương tiện vận chuyển",
                    onClick = { onNavigateToSection("VEHICLE") }
                )

                ProfileMenuItem(
                    icon = Icons.Default.Payments,
                    iconContainerColor = Color(0xFFECFDF5),
                    iconTint = Color(0xFF059669),
                    title = "Payout Settings",
                    subtitle = "Lịch sử thanh toán & Ngân hàng",
                    onClick = { onNavigateToSection("PAYOUT") }
                )

                ProfileMenuItem(
                    icon = Icons.Default.SupportAgent,
                    iconContainerColor = Color(0xFFFFF1F2),
                    iconTint = Color(0xFFE11D48),
                    title = "Support",
                    subtitle = "Trung tâm hỗ trợ 24/7",
                    onClick = { onNavigateToSection("SUPPORT") }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 🌟 NÚT ĐĂNG XUẤT (Nền đỏ nhạt, Chữ đỏ sậm nổi bật)
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(16.dp),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Đăng xuất", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}