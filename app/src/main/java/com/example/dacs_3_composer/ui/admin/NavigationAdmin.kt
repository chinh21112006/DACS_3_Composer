package com.example.dacs_3_composer.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationAdmin(val route: String, val title: String, val icon: ImageVector) {
    object Overview : NavigationAdmin("super_admin_overview", "Tổng quan", Icons.Default.InsertChart)
    object Orders : NavigationAdmin("super_admin_orders", "Đơn hàng", Icons.Default.Assignment)
    object Shippers : NavigationAdmin("super_admin_shippers", "Shiper", Icons.Default.Moped) // Icon & tên khớp nhãn "Cửa hàng" trên UI đại diện cho quản lý đối tác
    object Categories : NavigationAdmin("super_admin_categories", "Thực đơn", Icons.Default.Category) // Đại diện cho Quản lý danh mục thực đơn tổng
    object Customers : NavigationAdmin("super_admin_customers", "Cá nhân", Icons.Default.People) // Quản lý khách hàng / tài khoản cá nhân
}