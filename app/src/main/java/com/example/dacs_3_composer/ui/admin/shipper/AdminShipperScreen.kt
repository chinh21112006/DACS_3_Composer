package com.example.dacs_3_composer.ui.admin.shipper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.ui.admin.shipper.components.*

@Composable
fun AdminShipperScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Quản lý Shipper",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C1D)
                    )
                    Text(
                        text = "Giám sát hoạt động và điều phối đội ngũ giao hàng",
                        fontSize = 14.sp,
                        color = Color(0xFF727785)
                    )
                }
            }

            // Nút thêm mới được đưa vào đây thay vì TopBar
            item {
                Button(
                    onClick = { /* TODO: Thêm shipper */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2159BC)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PersonAddAlt1, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(text = "Thêm Shipper mới", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            item { ShipperStatsGrid() }
            item { ShipperSearchAndFilter() }
            item {
                ShipperCardItem(
                    name = "Nguyễn Minh Tuấn",
                    location = "Quận 1, TP.HCM",
                    orderCount = "1,240",
                    rating = "4.9",
                    status = "ACTIVE"
                )
            }
            item {
                ShipperCardItem(
                    name = "Trần Thị Lan Anh",
                    location = "Quận 7, TP.HCM",
                    orderCount = "856",
                    rating = "4.7",
                    status = "DELIVERING"
                )
            }
            item { ShipperPagination() }
        }
    }
}