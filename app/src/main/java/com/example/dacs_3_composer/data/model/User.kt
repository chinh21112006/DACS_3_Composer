package com.example.dacs_3_composer.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val role: String = "",
    val vehicleName: String = "",
    val isAvailable: Boolean = false,
    // 🎯 THÊM DÒNG NÀY VÀO: Mặc định tài khoản mới tạo sẽ là ACTIVE
    val status: String = "ACTIVE"
)