package com.example.dacs_3_composer.data.repository

import com.example.dacs_3_composer.data.model.ActivityLog
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun logActivity(
        restaurantId: String,
        type: String,
        title: String,
        description: String,
        details: String? = null,
        imageUrl: String? = null,
        extraInfo: Map<String, String>? = null
    ) {
        if (restaurantId.isBlank()) return

        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        // Fix deprecated Locale constructor
        val localeVi = Locale.forLanguageTag("vi-VN")
        val dateFormat = SimpleDateFormat("dd 'Tháng' MM", localeVi)
        
        // Tự động xác định nhãn ngày (Hôm nay, Hôm qua, hoặc ngày cụ thể)
        val dateLabel = "Hôm nay, " + dateFormat.format(calendar.time)

        val log = ActivityLog(
            title = title,
            description = description,
            time = timeFormat.format(calendar.time),
            date = dateLabel,
            timestamp = System.currentTimeMillis(),
            type = type,
            details = details,
            imageUrl = imageUrl,
            extraInfo = extraInfo
        )

        firestore.collection("restaurants")
            .document(restaurantId)
            .collection("activity_logs")
            .add(log)
    }
}
