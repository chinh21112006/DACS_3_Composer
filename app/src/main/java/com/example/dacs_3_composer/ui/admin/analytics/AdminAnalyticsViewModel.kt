package com.example.dacs_3_composer.ui.admin.analytics

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminAnalyticsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _adminInfo = MutableStateFlow<User?>(null)
    val adminInfo: StateFlow<User?> = _adminInfo.asStateFlow()

    init {
        observeAdminInfo()
    }

    private fun observeAdminInfo() {
        val uid = auth.currentUser?.uid ?: return
        
        firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminAnalyticsVM", "Error fetching admin info", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(uid = uid)
                    _adminInfo.value = user
                }
            }
    }
}
