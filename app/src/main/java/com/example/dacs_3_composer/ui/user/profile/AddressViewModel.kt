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

    // 🔄 TẢI MẢNG savedAddresses TỪ TRONG DOCUMENT USER
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
                    // Lấy mảng savedAddresses ra dưới dạng List các Map
                    val rawAddresses = snapshot.get("savedAddresses") as? List<Map<String, Any>>
                    if (rawAddresses != null) {
                        val parsedList = rawAddresses.map { map ->
                            UserAddress(
                                name = map["name"] as? String ?: "",
                                phone = map["phone"] as? String ?: "",
                                address = map["address"] as? String ?: ""
                            )
                        }
                        _addressList.value = parsedList
                    } else {
                        _addressList.value = emptyList()
                    }
                }
            }
    }

    // ➕ THÊM ĐỊA CHỈ MỚI VÀO MẢNG
    fun addAddress(newAddress: UserAddress, onComplete: () -> Unit) {
        val userDocRef = firestore.collection("users").document(userId)

        // Chuyển Object thành Map đúng định dạng Firebase mong muốn
        val addressMap = mapOf(
            "name" to newAddress.name,
            "phone" to newAddress.phone,
            "address" to newAddress.address
        )

        // Sử dụng arrayUnion để đẩy thêm 1 Map vào mảng savedAddresses có sẵn
        userDocRef.update("savedAddresses", FieldValue.arrayUnion(addressMap))
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { e -> Log.e("AddressViewModel", "Lỗi thêm địa chỉ vào mảng", e) }
    }

    // ❌ XÓA ĐỊA CHỈ KHỎI MẢNG
    fun deleteAddress(addressToDelete: UserAddress) {
        val userDocRef = firestore.collection("users").document(userId)

        val addressMap = mapOf(
            "name" to addressToDelete.name,
            "phone" to addressToDelete.phone,
            "address" to addressToDelete.address
        )

        // Sử dụng arrayRemove để tìm đúng Map này trong mảng và xóa nó đi
        userDocRef.update("savedAddresses", FieldValue.arrayRemove(addressMap))
            .addOnFailureListener { e -> Log.e("AddressViewModel", "Lỗi xóa địa chỉ khỏi mảng", e) }
    }

    // ✏️ SỬA ĐỊA CHỈ (Trong mảng Firestore: Cách tốt nhất là xóa Map cũ đi và nạp Map mới vào)
    fun updateAddress(oldAddress: UserAddress, newAddress: UserAddress, onComplete: () -> Unit) {
        val userDocRef = firestore.collection("users").document(userId)

        val oldMap = mapOf("name" to oldAddress.name, "phone" to oldAddress.phone, "address" to oldAddress.address)
        val newMap = mapOf("name" to newAddress.name, "phone" to newAddress.phone, "address" to newAddress.address)

        firestore.runTransaction { transaction ->
            transaction.update(userDocRef, "savedAddresses", FieldValue.arrayRemove(oldMap))
            transaction.update(userDocRef, "savedAddresses", FieldValue.arrayUnion(newMap))
        }.addOnSuccessListener {
            onComplete()
        }.addOnFailureListener { e ->
            Log.e("AddressViewModel", "Lỗi cập nhật phần tử mảng", e)
        }
    }
}