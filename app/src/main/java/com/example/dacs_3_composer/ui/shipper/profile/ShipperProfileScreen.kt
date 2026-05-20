package com.example.dacs_3_composer.ui.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dacs_3_composer.ui.shipper.profile.components.ProfileMenuItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperProfileScreen(
    onLogoutClick: () -> Unit,
    onNavigateToSection: (String) -> Unit,
    viewModel: ShipperProfileViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val user by viewModel.user.collectAsState()

    val totalOrders by viewModel.totalOrders.collectAsState()
    val totalEarnings by viewModel.totalEarnings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
            ) {}
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        if (isLoading && user.uid.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Driver Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            modifier = Modifier.size(50.dp),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.name.ifBlank { "Tài xế Gourmet" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Text(
                        text = " 4.9  •  ${user.vehicleName.ifBlank { "Chưa cập nhật xe" }}",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ô 1: Tổng đơn hàng đã giao thành công
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "ĐƠN HOÀN THÀNH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$totalOrders đơn", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D4ED8))
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                        }
                    }

                    // Ô 2: Tổng thu nhập Shipper từ trước đến nay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "TỔNG THU NHẬP SHIPPER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Ép kiểu sang Long để loại bỏ phần thập phân lẻ của VND
                            val formattedEarnings = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(totalEarnings.toLong())
                            Text(
                                text = "$formattedEarnings đ",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payments, null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CÀI ĐẶT TÀI KHOẢN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        iconContainerColor = Color(0xFFEEF2FF),
                        iconTint = Color(0xFF4F46E5),
                        title = "Account Info",
                        subtitle = "Chỉnh sửa Ảnh, Tên, SĐT, Địa chỉ & Loại xe",
                        onClick = { onNavigateToSection("EDIT_PROFILE") }
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.SupportAgent,
                        iconContainerColor = Color(0xFFFFF1F2),
                        iconTint = Color(0xFFE11D48),
                        title = "Support",
                        subtitle = "Trung tâm hỗ trợ tài xế 24/7",
                        onClick = { onNavigateToSection("SUPPORT") }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { onLogoutClick() },
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
}