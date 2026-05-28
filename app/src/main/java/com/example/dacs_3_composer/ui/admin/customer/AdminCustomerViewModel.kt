package com.example.dacs_3_composer.ui.admin.customer

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminCustomerViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _customers = MutableStateFlow<List<User>>(emptyList())
    val customers: StateFlow<List<User>> = _customers

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Quản lý bộ lọc vai trò được chọn trên TopBar ("all", "admin", "restaurant", "user", "shipper")
    private val _selectedRoleFilter = MutableStateFlow("all")
    val selectedRoleFilter: StateFlow<String> = _selectedRoleFilter

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    init {
        observeCustomers()
    }

    private fun observeCustomers() {
        firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminCustomerVM", "Lỗi tải dữ liệu người dùng: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<User>()
                    for (doc in snapshot.documents) {
                        try {
                            val uid = doc.id
                            val name = doc.getString("name") ?: ""
                            val phone = doc.getString("phone") ?: ""
                            val email = doc.getString("email") ?: ""
                            val address = doc.getString("address") ?: ""
                            val role = doc.getString("role") ?: "user"
                            val avatarUrl = doc.getString("avatarUrl") ?: ""
                            val vehicleName = doc.getString("vehicleName") ?: ""

                            val isAvailable = doc.getBoolean("isAvailable")
                                ?: (doc.getString("status") != "LOCKED")

                            val user = User(
                                uid = uid,
                                name = name,
                                phone = phone,
                                email = email,
                                address = address,
                                role = role,
                                avatarUrl = avatarUrl,
                                vehicleName = vehicleName,
                                isAvailable = isAvailable
                            )
                            list.add(user)
                        } catch (e: Exception) {
                            Log.e("AdminCustomerVM", "Lỗi ép kiểu Document: ${e.message}")
                        }
                    }
                    _customers.value = list
                    _totalCount.value = list.size
                }
            }
    }

    // Cập nhật từ khóa tìm kiếm
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Cập nhật bộ lọc vai trò (Tab được nhấn)
    fun onRoleFilterChanged(role: String) {
        _selectedRoleFilter.value = role.lowercase()
    }

    // THÊM tài khoản mới
    fun addCustomer(name: String, phone: String, email: String, address: String, role: String, vehicleName: String) {
        if (email.isBlank() || phone.isBlank()) return

        auth.createUserWithEmailAndPassword(email, phone)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                val newCustomer = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "phone" to phone,
                    "email" to email,
                    "address" to address,
                    "role" to role,
                    "vehicleName" to vehicleName,
                    "isAvailable" to true,
                    "status" to "ACTIVE",
                    "avatarUrl" to ""
                )
                firestore.collection("users").document(uid).set(newCustomer)
            }
            .addOnFailureListener { e ->
                Log.e("AdminCustomerVM", "Lỗi không thể tạo Auth: ${e.message}")
            }
    }

    // SỬA thông tin tài khoản hiện có
    fun updateCustomer(uid: String, name: String, phone: String, email: String, address: String, role: String, vehicleName: String) {
        if (uid.isBlank()) return

        val updates = mapOf(
            "name" to name,
            "phone" to phone,
            "email" to email,
            "address" to address,
            "role" to role,
            "vehicleName" to vehicleName
        )

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnFailureListener { e ->
                Log.e("AdminCustomerVM", "Lỗi cập nhật Firestore: ${e.message}")
            }
    }

    // XÓA tài khoản khỏi hệ thống
    fun deleteCustomer(uid: String) {
        if (uid.isBlank()) return
        firestore.collection("users").document(uid).delete()
    }

    // XỬ LÝ KHÓA / MỞ KHÓA
    fun toggleLockStatus(uid: String, currentAvailable: Boolean) {
        if (uid.isBlank()) return

        val nextAvailableState = !currentAvailable
        val nextStatusString = if (nextAvailableState) "ACTIVE" else "LOCKED"

        val updates = mapOf(
            "isAvailable" to nextAvailableState,
            "status" to nextStatusString
        )

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                Log.d("AdminCustomerVM", "Đã cập nhật khóa thành công!")
            }
            .addOnFailureListener { e ->
                Log.e("AdminCustomerVM", "Lỗi Firebase: ${e.message}")
            }
    }
}