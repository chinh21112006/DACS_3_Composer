package com.example.dacs_3_composer

import android.app.Application
import com.cloudinary.android.MediaManager

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Chỉ cần cloud_name cho Unsigned Upload
        val config = mapOf(
            "cloud_name" to "dhscw17vq", 
            "secure" to true
        )
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Tránh lỗi khởi tạo lại nếu App không bị kill hẳn
        }
    }
}
