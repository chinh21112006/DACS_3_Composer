package com.example.dacs_3_composer.ui.restaurant.profile.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs_3_composer.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantInfoScreen(
    onBackClick: () -> Unit,
    viewModel: RestaurantInfoViewModel = viewModel()
) {
    val detail = viewModel.restaurantDetail
    
    // State cho form
    var name by remember(detail) { mutableStateOf(detail?.name ?: "") }
    var email by remember(detail) { mutableStateOf(detail?.email ?: "") }
    var phone by remember(detail) { mutableStateOf(detail?.phone ?: "") }
    var address by remember(detail) { mutableStateOf(detail?.address ?: "") }
    var openTime by remember(detail) { mutableStateOf(detail?.openTime ?: "") }
    var closeTime by remember(detail) { mutableStateOf(detail?.closeTime ?: "") }
    var description by remember(detail) { mutableStateOf(detail?.description ?: "") }
    
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCoverUri by remember { mutableStateOf<Uri?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedAvatarUri = it }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedCoverUri = it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FF))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // COVER IMAGE SECTION
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(selectedCoverUri ?: detail?.coverImage ?: R.drawable.banner1)
                    .crossfade(true).build(),
                contentDescription = "Cover Image",
                modifier = Modifier.fillMaxSize().clickable { coverPicker.launch("image/*") },
                contentScale = ContentScale.Crop
            )
            
            // Back Button Overlay
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            // Edit Cover Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable { coverPicker.launch("image/*") }
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Sửa ảnh bìa", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // AVATAR SECTION (Overlapping)
        Box(modifier = Modifier.offset(y = (-55).dp)) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedAvatarUri ?: detail?.avatarUrl ?: R.drawable.banner1)
                        .crossfade(true).build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .clickable { avatarPicker.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
                // Edit Avatar Button
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1A73E8), CircleShape)
                        .clickable { avatarPicker.launch("image/*") }
                        .padding(6.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa avatar", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-30).dp)) {
            InfoTextField(label = "Tên cửa hàng", value = name, onValueChange = { name = it }, icon = Icons.Default.Storefront)
            InfoTextField(label = "Email liên hệ", value = email, onValueChange = { email = it }, icon = Icons.Default.Mail)
            InfoTextField(label = "Số điện thoại", value = phone, onValueChange = { phone = it }, icon = Icons.Default.Call)
            InfoTextField(label = "Địa chỉ", value = address, onValueChange = { address = it }, icon = Icons.Default.LocationOn)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    InfoTextField(label = "Giờ mở cửa", value = openTime, onValueChange = { openTime = it }, icon = Icons.Default.Schedule)
                }
                Box(modifier = Modifier.weight(1f)) {
                    InfoTextField(label = "Giờ đóng cửa", value = closeTime, onValueChange = { closeTime = it }, icon = Icons.Default.HistoryToggleOff)
                }
            }

            InfoTextField(label = "Mô tả nhà hàng", value = description, onValueChange = { description = it }, icon = Icons.Default.Description, singleLine = false, minLines = 3)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updateRestaurantInfo(name, email, phone, address, openTime, closeTime, description, selectedAvatarUri, selectedCoverUri)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.saveSuccess) Color(0xFF008939) else Color(0xFF1A73E8)),
                enabled = !viewModel.isSaving
            ) {
                if (viewModel.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text(if (viewModel.saveSuccess) "Đã lưu thành công" else "Lưu thay đổi", fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            LaunchedEffect(viewModel.saveSuccess) {
                if (viewModel.saveSuccess) {
                    delay(2000)
                    viewModel.resetSaveSuccess()
                }
            }
            Spacer(modifier = Modifier.height(100.dp)) // Padding cho bottom nav
        }
    }
}

@Composable
fun InfoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        if (label.isNotEmpty()) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF414754),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF727785)) },
            placeholder = { Text(placeholder, color = Color(0xFF727785)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFC1C6D6),
                focusedBorderColor = Color(0xFF005BBF),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color(0xFF181C20),
                focusedTextColor = Color(0xFF181C20)
            ),
            singleLine = singleLine,
            minLines = minLines
        )
    }
}
