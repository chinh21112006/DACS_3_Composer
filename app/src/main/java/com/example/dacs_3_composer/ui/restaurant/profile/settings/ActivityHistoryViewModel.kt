package com.example.dacs_3_composer.ui.restaurant.profile.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.ActivityLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ActivityHistoryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ActivityHistoryVM"

    private val restaurantId: String
        get() = auth.currentUser?.uid ?: ""

    var activityLogs by mutableStateOf<List<ActivityLog>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    init {
        fetchActivityLogs()
    }

    private fun fetchActivityLogs() {
        if (restaurantId.isBlank()) return
        
        isLoading = true
        // 🎯 SỬA: Sắp xếp theo timestamp để chính xác về mặt thời gian
        firestore.collection("restaurants")
            .document(restaurantId)
            .collection("activity_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    Log.e(TAG, "Error listening to activity logs", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    activityLogs = snapshot.toObjects(ActivityLog::class.java)
                    Log.d(TAG, "Fetched ${activityLogs.size} activity logs")
                }
            }
    }
}
