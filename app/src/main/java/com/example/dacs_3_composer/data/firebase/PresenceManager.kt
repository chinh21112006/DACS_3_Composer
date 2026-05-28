package com.example.dacs_3_composer.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

class PresenceManager(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUid get() = auth.currentUser?.uid

    fun updateUserStatus(isOnline: Boolean) {
        val uid = currentUid ?: return
        val userStatus = mapOf(
            "isOnline" to isOnline,
            "lastSeen" to FieldValue.serverTimestamp()
        )
        firestore.collection("users").document(uid)
            .set(userStatus, SetOptions.merge())
    }

    fun startTracking() {
        updateUserStatus(true)
    }

    fun stopTracking() {
        updateUserStatus(false)
    }
}
