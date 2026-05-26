package com.example.dacs_3_composer.ui.admin.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.ui.admin.category.components.*

@Composable
fun AdminCategoryScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = Color(0xFF0052CC), contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Quản lý Danh mục",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C1D)
                    )
                    Text(
                        text = "Tạo và điều chỉnh các nhóm thực đơn dùng chung cho toàn hệ thống nhà hàng.",
                        fontSize = 14.sp,
                        color = Color(0xFF727785)
                    )
                }
            }

            item { CategorySearchBar() }

            item {
                CategoryCardItem(
                    title = "Món chính",
                    description = "Các món ăn phục vụ chính trong bữa ăn.",
                    itemCount = "42 món ăn",
                    icon = Icons.Default.LocalDining,
                    iconBgColor = Color(0xFFE8EFFF),
                    iconTint = Color(0xFF0052CC)
                )
            }

            item {
                CategoryCardItem(
                    title = "Khai vị",
                    description = "Kích thích vị giác trước bữa tiệc.",
                    itemCount = "18 món ăn",
                    icon = Icons.Default.WineBar,
                    iconBgColor = Color(0xFFFCE8E6),
                    iconTint = Color(0xFFA94442)
                )
            }

            item {
                CategoryCardItem(
                    title = "Đồ uống",
                    description = "Nước giải khát, rượu và cocktail.",
                    itemCount = "35 món ăn",
                    icon = Icons.Default.LocalBar,
                    iconBgColor = Color(0xEFE8FFFF),
                    iconTint = Color(0xFF673AB7)
                )
            }

            item {
                CategoryCardItem(
                    title = "Tráng miệng",
                    description = "Kết thúc bữa ăn với sự ngọt ngào.",
                    itemCount = "24 món ăn",
                    icon = Icons.Default.Cake,
                    iconBgColor = Color(0xFFE8F8F0),
                    iconTint = Color(0xFF2ECC71)
                )
            }

            item { AddCategoryDashedButton() }
        }
    }
}