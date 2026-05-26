package com.example.dacs_3_composer.ui.admin.shipper.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs_3_composer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperTopBar(
    adminName: String = "Gourmet Admin",
    avatarUrl: String = "",
    onAddShipperClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl.ifEmpty { R.drawable.banner1 })
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() },
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.banner1),
                error = painterResource(id = R.drawable.banner1)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = adminName.ifEmpty { "Gourmet Admin" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1D),
                modifier = Modifier.weight(1f).clickable { onAvatarClick() }
            )
            IconButton(onClick = {}) {
                BadgedBox(badge = { Badge(containerColor = Color.Red) }) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = Color(0xFF2159BC))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Quản lý Shipper", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1D))
        Text(text = "Giám sát hoạt động và điều phối đội ngũ giao hàng", fontSize = 14.sp, color = Color(0xFF727785))
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddShipperClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PersonAddAlt1, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(text = "Thêm Shipper mới", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}