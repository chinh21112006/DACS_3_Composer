package com.example.dacs_3_composer.ui.user.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs_3_composer.R

@Composable
fun Header(
    userName: String,
    userAddress: String,
    userImageUrl: String,
    onSearchAction: (String) -> Unit,
    onMessageClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(bottom = 16.dp)
    ) {
        // Hàng 1: Top Bar đồng bộ với Admin/Restaurant
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = userImageUrl.ifBlank { R.drawable.ic_avatar_default },
                placeholder = painterResource(id = R.drawable.ic_avatar_default),
                error = painterResource(id = R.drawable.ic_avatar_default),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Xin chào,",
                    fontSize = 12.sp,
                    color = Color(0xFF727785)
                )
                Text(
                    text = userName.ifBlank { "Khách hàng" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2159BC)
                )
            }

            IconButton(onClick = onMessageClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Tin nhắn",
                    tint = Color(0xFF2159BC),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = { Badge(containerColor = Color.Red, modifier = Modifier.size(8.dp)) }
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Thông báo",
                        tint = Color(0xFF2159BC),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Hàng 2: Địa chỉ
        Text(
            text = if (userAddress.isNotBlank()) "📍 $userAddress" else "📍 Chưa cập nhật địa chỉ",
            fontSize = 13.sp,
            color = Color(0xFF727785),
            maxLines = 1,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hàng 3: Ô tìm kiếm
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm kiếm món ăn, nhà hàng...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F3F4),
                unfocusedContainerColor = Color(0xFFF1F3F4),
                disabledContainerColor = Color(0xFFF1F3F4),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        onSearchAction(searchQuery)
                    }
                }
            )
        )
    }
}