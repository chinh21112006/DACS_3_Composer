package com.example.dacs_3_composer

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.dacs_3_composer.data.backend.PayOSServer

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Khởi tạo Backend Ktor ngay trong App
        PayOSServer.start()

        // Khởi tạo Cloudinary
        val config = mapOf(
            "cloud_name" to "dhscw17vq", 
            "secure" to true
        )
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) { }
    }
}
