package com.example.dacs_3_composer.ui.admin.shipper.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperSearchAndFilter() {
    var activeTab by remember { mutableStateOf(0) }
    val filterTabs = listOf("Tất cả", "Đang giao", "Nghỉ phép", "Bị khóa")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thanh Tìm kiếm
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Tìm tên, ID hoặc khu vực...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F3F4),
                unfocusedContainerColor = Color(0xFFF1F3F4),
                disabledContainerColor = Color(0xFFF1F3F4),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            )
        )

        // Hàng bộ lọc có thể cuộn ngang
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterTabs.forEachIndexed { index, title ->
                val isSelected = activeTab == index
                FilterChip(
                    selected = isSelected,
                    onClick = { activeTab = index },
                    label = { Text(title) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0052CC),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFE8EFFF),
                        labelColor = Color(0xFF555555)
                    ),
                    border = null
                )
            }
        }
    }
}