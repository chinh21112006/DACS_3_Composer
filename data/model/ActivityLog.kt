package com.example.dacs_3_composer.data.model

import com.google.firebase.firestore.DocumentId

data class ActivityLog(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val time: String = "", // Ví dụ: "08:15 AM"
    val date: String = "", // Ví dụ: "24 Tháng 5"
    val timestamp: Long = 0L, // Trường quan trọng để sắp xếp
    val type: String = "info", // "login", "menu", "profile", "order", "security"
    val details: String? = null,
    val imageUrl: String? = null,
    val extraInfo: Map<String, String>? = null
)
