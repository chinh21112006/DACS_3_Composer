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
    object Categories : NavigationAdmin("super_admin_categories", "Thực đơn", Icons.Default.Category)
    object Customers : NavigationAdmin("super_admin_customers", "All Role", Icons.Default.People)
    object Profile : NavigationAdmin("super_admin_profile", "Profile", Icons.Default.Moped)

}