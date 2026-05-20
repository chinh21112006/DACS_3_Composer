package com.example.dacs_3_composer.data.model

data class UserAddress(
    val id: String = java.util.UUID.randomUUID().toString(), // Tạo ID ngẫu nhiên để Jetpack Compose nhận diện key
    val name: String = "",
    val phone: String = "",
    val address: String = "" // Đổi từ detailAddress sang address cho khớp Firebase của bạn
)