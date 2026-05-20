package com.example.dacs_3_composer.ui.user.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs_3_composer.data.model.Dish

//          Vẽ giao diện có colum bao quanh có ảnh tên..vv
@Composable
fun DeliciousDishItem(dish: Dish,onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .padding(bottom = 8.dp)
    ) {
        // Ảnh món ăn bo góc vuông lớn giống ảnh mẫu
        AsyncImage(
            model = dish.imageUrl, // Link ảnh từ Firestore
            contentDescription = dish.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(18.dp)), // Bo góc sâu giống thiết kế
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Tên món ăn viết đậm (Ví dụ: Chân gà nướng)
        Text(
            text = dish.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Sau này khi làm thật, bạn có thể truyền thêm thuộc tính từ bảng Restaurant vào đây.
        Text(
            text = "Hai Còi - ${dish.name} | 5.2 km - ⭐ 4.8 ",
            style = MaterialTheme.typography.bodySmall,
            color = androidx.compose.ui.graphics.Color.Gray,
            maxLines = 2, // Cho phép hiển thị tối đa 2 dòng giống ảnh mẫu
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp // Khoảng cách giữa 2 dòng chữ nhỏ cho dễ đọc
        )
    }
}