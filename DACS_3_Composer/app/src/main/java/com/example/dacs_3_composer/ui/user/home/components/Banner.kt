package com.example.dacs_3_composer.ui.user.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
// Đảm bảo R file là đúng của dự án bạn
import com.example.dacs_3_composer.R

@Composable
fun Banner() {
    // 1. Sử dụng Card để dễ dàng bo góc và tạo khoảng cách
    Card(
        modifier = Modifier
            .fillMaxWidth() // Chiều rộng đầy đủ
            .height(300.dp), // 2. Tăng chiều cao để ảnh "to hơn" (trước là 200.dp)

        shape = RoundedCornerShape(25.dp), // 4. Định nghĩa độ bo góc
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Tùy chọn: Thêm bóng đổ nhẹ
    ) {
        Image(
            painter = painterResource(id = R.drawable.nha_hang),
            contentDescription = "Hình ảnh quảng cáo",
            modifier = Modifier.fillMaxWidth(), // Ảnh chiếm hết chiều rộng của Card
            // 5. Quan trọng: scale ảnh để lấp đầy Card mà không bị méo
            contentScale = ContentScale.Crop
        )
    }
}