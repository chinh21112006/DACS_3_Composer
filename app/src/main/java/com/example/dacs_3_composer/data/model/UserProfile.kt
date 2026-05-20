package com.example.dacs_3_composer.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "user", // "user", "admin", hoặc "shipper"
    val phone: String = "",    // Nếu Firestore chưa có trường này, Kotlin tự gán = ""
    val address: String = "",  // Nếu Firestore chưa có trường này, Kotlin tự gán = ""
    val savedAddresses: List<Map<String, String>> = emptyList()
)