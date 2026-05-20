package com.example.dacs_3_composer.ui.user.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.ui.user.profile.components.MembershipAndWalletSection
import com.example.dacs_3_composer.ui.user.profile.components.ProfileHeader
import com.example.dacs_3_composer.ui.user.profile.components.ProfileMenuRow

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel(), // 🌟 Nhúng bộ xử lý dữ liệu vào
    onLogoutClick: () -> Unit = {},
    onNavigateToOrderHistory: () -> Unit = {}, // 🌟 Thêm nhận lambda
    onNavigateToManageAddress: () -> Unit = {}  // 🌟 Thêm nhận lambda
) {
    val scrollState = rememberScrollState()

    // Tự động load dữ liệu Tên, Email, Ảnh đại diện từ Firebase khi mở màn hình này lên
    LaunchedEffect(Unit) {
        profileViewModel.loadUserData()
    }

    // 🚀 ĐĂNG KÝ BỘ CHỌN ẢNH TỪ THƯ VIỆN MÁY
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Nếu người dùng chọn ảnh thành công, gửi uri này lên Firebase xử lý luôn
        uri?.let { profileViewModel.uploadAvatar(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Gọi Component Header cá nhân (Dữ liệu lấy động từ ViewModel)
            // 1. Gọi Component Header cá nhân
            ProfileHeader(
                name = profileViewModel.userName,
                email = profileViewModel.userEmail,
                avatarState = profileViewModel.avatarState, // 🌟 Thay đổi truyền trạng thái mới ở đây
                onEditAvatarClick = {
                    // Kích hoạt mở thư viện ảnh của thiết bị để lựa chọn
                    imagePickerLauncher.launch("image/*")
                }
            )

            // 2. Gọi Component Thẻ Thành viên & Ví
            MembershipAndWalletSection(
                memberRank = "Gold Member",
                walletBalance = "1.250k"
            )

            // 3. Khối Danh sách Menu bo góc màu trắng
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ProfileMenuRow(
                        icon = Icons.Default.ReceiptLong,
                        iconTint = Color(0xFF2159BC),
                        iconBgColor = Color(0xFFE8F0FE),
                        title = "Lịch sử đơn hàng",
                        onClick = { onNavigateToOrderHistory() }
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.LocationOn,
                        iconTint = Color(0xFF2159BC),
                        iconBgColor = Color(0xFFE8F0FE),
                        title = "Địa chỉ đã lưu",
                        onClick = { onNavigateToManageAddress() }
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Payment,
                        iconTint = Color(0xFF2159BC),
                        iconBgColor = Color(0xFFE8F0FE),
                        title = "Phương thức thanh toán",
                        onClick = { /* Đi đến ví/thẻ */ }
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Settings,
                        iconTint = Color(0xFF2159BC),
                        iconBgColor = Color(0xFFE8F0FE),
                        title = "Cài đặt",
                        onClick = { /* Cài đặt app */ }
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.HelpOutline,
                        iconTint = Color(0xFF2159BC),
                        iconBgColor = Color(0xFFE8F0FE),
                        title = "Hỗ trợ",
                        onClick = { /* Đi đến trang trợ giúp */ }
                    )
                }
            }

            // 4. Nút Đăng xuất ở dưới cùng
            Button(
                onClick = { onLogoutClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F3F4)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFC0392B)
                    )
                    Text(text = "Đăng xuất", color = Color(0xFFC0392B), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hiện vòng tải xoay nhẹ đè lên màn hình khi ảnh đang được upload lên Firebase Storage
        if (profileViewModel.isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}