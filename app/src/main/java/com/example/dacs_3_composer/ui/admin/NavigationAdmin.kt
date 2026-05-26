package com.example.dacs_3_composer.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationAdmin(val route: String, val title: String, val icon: ImageVector) {
    object Overview : NavigationAdmin("super_admin_overview", "Tổng quan", Icons.Default.InsertChart)
    object Orders : NavigationAdmin("super_admin_orders", "Đơn hàng", Icons.Default.Assignment)
    object Shippers : NavigationAdmin("super_admin_shippers", "Shiper", Icons.Default.Moped)
    object Categories : NavigationAdmin("super_admin_categories", "Thực đơn", Icons.Default.Category)
    object Customers : NavigationAdmin("super_admin_customers", "Người dùng", Icons.Default.People)
    object Profile : NavigationAdmin("super_admin_profile", "Hồ sơ", Icons.Default.Person)
    object Promotions : NavigationAdmin("super_admin_promotions", "Khuyến mãi", Icons.Default.ConfirmationNumber)
}