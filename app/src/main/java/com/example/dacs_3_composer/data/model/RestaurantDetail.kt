package com.example.dacs_3_composer.data.model

import com.google.firebase.firestore.DocumentId

data class RestaurantDetail(
    @DocumentId
    var id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val address: String = "",
    val deliveryTime: String = "0 min",
    val distance: String = "0 km",
    val description: String = "",
    val coverImage: String = ""
)
