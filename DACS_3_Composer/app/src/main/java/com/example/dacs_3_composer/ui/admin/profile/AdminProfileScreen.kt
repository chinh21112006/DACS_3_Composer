package com.example.dacs_3_composer.ui.admin.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.ui.admin.profile.components.AccountInfoItem
import com.example.dacs_3_composer.ui.admin.profile.components.ProfileHeader
import com.example.dacs_3_composer.ui.admin.profile.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    viewModel: AdminProfileViewModel = viewModel(),
    onNavigateToVehicleManagement: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onLogoutCallbackk: () -> Unit
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // State quản lý việc ẩn/hiển thị Dialog sửa thông tin
    var showEditDialog by remember { mutableStateOf(false) }

    // State lưu giá trị tạm thời trong các ô nhập dữ liệu (TextField)
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    // ✅ ĐỒNG BỘ CHUẨN: Đổ chính xác dữ liệu của tài khoản đang đăng nhập hiện tại vào Form
    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            editName = uiState.adminName
            editEmail = uiState.adminEmail
            editPhone = uiState.adminPhone
        }
    }

    // Bộ chọn xử lý hình ảnh đại diện từ máy thiết bị gửi lên Cloudinary
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Đang tải ảnh lên hệ thống...", Toast.LENGTH_SHORT).show()
            viewModel.uploadAvatarToCloudinary(
                context = context,
                fileUri = uri,
                onSuccess = { Toast.makeText(context, "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show() },
                onFailure = { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
    ) {
        // 1. Header hiển thị tên & ảnh đại diện admin
        ProfileHeader(
            adminName = uiState.adminName,
            avatarUrl = uiState.avatarUrl,
            onEditAvatarClick = { imagePickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Khu vực hiển thị Thống kê 2 ô song song
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Tổng người dùng",
                value = uiState.totalUsersCount,
                icon = Icons.Default.SupervisorAccount,
                iconTint = Color(0xFF0052CC),
                valueColor = Color(0xFF0052CC),
                backgroundColor = Color(0xFFE6F0FF),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Trạng thái hệ thống",
                value = uiState.systemStatus,
                icon = Icons.Default.VerifiedUser,
                iconTint = Color(0xFF10B981),
                valueColor = Color(0xFF10B981),
                backgroundColor = Color(0xFFE6F9F0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "CÀI ĐẶT TÀI KHOẢN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 3. Danh sách Menu điều hướng cài đặt
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountInfoItem(
                title = "Account Info",
                subtitle = "Tên: ${uiState.adminName} | SĐT: ${uiState.adminPhone}",
                icon = Icons.Default.Person,
                iconBackgroundColor = Color(0xFFEEF2F6),
                iconTint = Color(0xFF4F46E5),
                onClick = { showEditDialog = true }
            )

            AccountInfoItem(
                title = "Vehicle Management",
                subtitle = "Quản lý phương tiện vận chuyển",
                icon = Icons.Default.Build,
                iconBackgroundColor = Color(0xFFF1F5F9),
                iconTint = Color(0xFF475569),
                onClick = onNavigateToVehicleManagement
            )

            AccountInfoItem(
                title = "Payout Settings",
                subtitle = "Lịch sử thanh toán & Ngân hàng",
                icon = Icons.Default.Payment,
                iconBackgroundColor = Color(0xFFE6F9F0),
                iconTint = Color(0xFF10B981),
                onClick = onNavigateToNotification
            )

            AccountInfoItem(
                title = "Support",
                subtitle = "Trung tâm hỗ trợ 24/7",
                icon = Icons.Default.Call,
                iconBackgroundColor = Color(0xFFFDF2F8),
                iconTint = Color(0xFFEC4899),
                onClick = onNavigateToSupport
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 4. Nút đăng xuất hệ thống
        Button(
            onClick = onLogoutCallbackk,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1F2)),
            shape = RoundedCornerShape(16.dp),
            elevation = null
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Đăng xuất", tint = Color(0xFFDC2626))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Đăng xuất", color = Color(0xFFDC2626), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // 🛠️ DIALOG CHỈNH SỬA THÔNG TIN CÁ NHÂN
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(text = "Chỉnh sửa thông tin", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Họ tên Admin") },
                        placeholder = { Text("Nhập họ tên mới") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Địa chỉ Email") },
                        placeholder = { Text("Nhập Email mới") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Số điện thoại") },
                        placeholder = { Text("Nhập số điện thoại mới") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank()) {
                            Toast.makeText(context, "Tên không được để trống!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Gọi hàm cập nhật dữ liệu của đúng Admin đang đăng nhập lên Firestore
                        viewModel.updateAccountInfo(
                            newName = editName.trim(),
                            newEmail = editEmail.trim(),
                            newPhone = editPhone.trim()
                        ) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, "Cập nhật dữ liệu thành công!", Toast.LENGTH_SHORT).show()
                                showEditDialog = false
                            } else {
                                Toast.makeText(context, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Lưu lại", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Hủy bỏ", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}