package com.example.dacs_3_composer.ui.user.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.UserAddress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AddressViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _addressList = MutableStateFlow<List<UserAddress>>(emptyList())
    val addressList: StateFlow<List<UserAddress>> = _addressList

    var isLoading by mutableStateOf(false)
        private set

    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        if (userId == "guest_user") return
        isLoading = true

        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    Log.e("AddressViewModel", "Lỗi tải mảng địa chỉ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val rawAddresses = snapshot.get("savedAddresses") as? List<Map<String, Any>>
                    if (rawAddresses != null) {
                        val parsedList = rawAddresses.map { map ->
                            UserAddress(
                                name = map["name"] as? String ?: "",
                                phone = map["phone"] as? String ?: "",
                                address = map["address"] as? String ?: "",
                                addressDetail = map["addressDetail"] as? String ?: "",
                                latitude = (map["latitude"] as? Number)?.toDouble() ?: 16.0748,
                                longitude = (map["longitude"] as? Number)?.toDouble() ?: 108.2240
                            )
                        }
                        _addressList.value = parsedList
                    } else {
                        _addressList.value = emptyList()
                    }
                }
            }
    }

    fun addAddress(newAddress: UserAddress, onComplete: () -> Unit) {
        val userDocRef = firestore.collection("users").document(userId)
        val addressMap = mapAddressToMap(newAddress)

        userDocRef.update("savedAddresses", FieldValue.arrayUnion(addressMap))
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { e -> Log.e("AddressViewModel", "Lỗi thêm địa chỉ", e) }
    }

    fun deleteAddress(addressToDelete: UserAddress) {
        val userDocRef = firestore.collection("users").document(userId)
        val addressMap = mapAddressToMap(addressToDelete)

        userDocRef.update("savedAddresses", FieldValue.arrayRemove(addressMap))
            .addOnFailureListener { e -> Log.e("AddressViewModel", "Lỗi xóa địa chỉ", e) }
    }

    fun updateAddress(oldAddress: UserAddress, newAddress: UserAddress, onComplete: () -> Unit) {
        val userDocRef = firestore.collection("users").document(userId)
        val oldMap = mapAddressToMap(oldAddress)
        val newMap = mapAddressToMap(newAddress)

        firestore.runTransaction { transaction ->
            transaction.update(userDocRef, "savedAddresses", FieldValue.arrayRemove(oldMap))
            transaction.update(userDocRef, "savedAddresses", FieldValue.arrayUnion(newMap))
        }.addOnSuccessListener {
            onComplete()
        }.addOnFailureListener { e ->
            Log.e("AddressViewModel", "Lỗi cập nhật địa chỉ", e)
        }
    }

    private fun mapAddressToMap(address: UserAddress): Map<String, Any> {
        return mapOf(
            "name" to address.name,
            "phone" to address.phone,
            "address" to address.address,
            "addressDetail" to address.addressDetail,
            "latitude" to address.latitude,
            "longitude" to address.longitude
        )
    }
}