package com.example.dacs_3_composer.ui.user.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dacs_3_composer.ui.user.home.HomeViewModel

//              Chứa vòng lặp items() để xếp các món ăn thành hàng ngang.
@Composable
fun DeliciousDishSection(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val allListDishes by viewModel.deliciousDishes.collectAsState()

    val row1Dishes = allListDishes

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        // Tiêu đề Section giống hệt ảnh mẫu của bạn
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Món ngon cho bạn",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Xem tất cả",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        // --- HÀNG 1 ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(row1Dishes) { dish ->
                // Đổi cấu trúc DeliciousDishItem nhận vào đối tượng Dish từ database luôn
                DeliciousDishItem(dish = dish, onClick = {
//                    Chuyển màn hình và đưa id nhà hàng
                    navController.navigate("restaurant_detail/${dish.restaurantId}")
                })
            }
        }
    }
}