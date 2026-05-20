package com.example.dacs_3_composer.data.model

import com.google.firebase.firestore.DocumentId

//Gọi để thêm sửa xóa cho firebase
data class Dish(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "Tất cả",
    val description: String = "",
    val available: Boolean = true,
    val restaurantId: String = ""
)
