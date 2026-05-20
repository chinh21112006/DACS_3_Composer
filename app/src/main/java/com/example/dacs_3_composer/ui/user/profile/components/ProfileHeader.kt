package com.example.dacs_3_composer.ui.user.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs_3_composer.R

@Composable
fun ProfileHeader(
    name: String,
    email: String,
    avatarState: Any?, // 🌟 SỬA TẠI ĐÂY: Nhận kiểu Any? (Chấp nhận cả Uri tệp điện thoại hoặc String URL)
    onEditAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = avatarState ?: R.drawable.ic_avatar_default, // Đưa dữ liệu tệp vào đây
                contentDescription = "Avatar",
                placeholder = painterResource(id = R.drawable.ic_avatar_default),
                error = painterResource(id = R.drawable.ic_avatar_default),
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(100.dp)),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = onEditAvatarClick,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF2159BC), CircleShape)
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Sửa ảnh",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1D))
        Text(text = email, fontSize = 14.sp, color = Color(0xFF727785), modifier = Modifier.padding(top = 4.dp))
    }
}