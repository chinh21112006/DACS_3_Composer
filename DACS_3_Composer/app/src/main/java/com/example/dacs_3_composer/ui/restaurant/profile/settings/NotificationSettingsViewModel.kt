package com.example.dacs_3_composer.ui.restaurant.profile.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.RestaurantDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationSettingsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "NotificationSettingsVM"

    private val restaurantId: String
        get() = auth.currentUser?.uid ?: ""

    var restaurantDetail by mutableStateOf<RestaurantDetail?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    init {
        fetchSettings()
    }

    private fun fetchSettings() {
        if (restaurantId.isBlank()) return
        
        firestore.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to settings", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    restaurantDetail = snapshot.toObject(RestaurantDetail::class.java)
                }
            }
    }

    fun updateNotificationSetting(field: String, value: Boolean) {
        if (restaurantId.isBlank()) return
        
        viewModelScope.launch {
            try {
                firestore.collection("restaurants").document(restaurantId)
                    .update(field, value)
                    .await()
                Log.d(TAG, "Updated $field to $value")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating $field", e)
            }
        }
    }
}
