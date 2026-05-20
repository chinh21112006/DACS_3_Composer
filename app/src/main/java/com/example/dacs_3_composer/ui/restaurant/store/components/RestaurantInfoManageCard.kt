package com.example.dacs_3_composer.ui.restaurant.store.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.data.model.RestaurantDetail

@Composable
fun RestaurantInfoManageCard(
    restaurant: RestaurantDetail,
    onEditInfoClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Tên nhà hàng
                    Text(
                        text = restaurant.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                // NÚT ICON CHỈNH SỬA THÔNG TIN (Tên, mô tả, địa chỉ)
                IconButton(
                    onClick = onEditInfoClick,
                    modifier = Modifier
                        .background(Color(0xFFF0F4F8), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Sửa thông tin quán",
                        tint = Color(0xFF1E56A0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Mô tả nhà hàng
            Text(
                text = restaurant.description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Địa chỉ nhà hàng
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(
                    painter = painterResource(id = com.example.dacs_3_composer.R.drawable.ic_location),
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = restaurant.address,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}