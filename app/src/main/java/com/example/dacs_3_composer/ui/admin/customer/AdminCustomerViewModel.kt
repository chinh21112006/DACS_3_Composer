package com.example.dacs_3_composer.ui.admin.customer

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.User // Hãy đảm bảo đường dẫn model User này chuẩn xác
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminCustomerViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _customers = MutableStateFlow<List<User>>(emptyList())
    val customers: StateFlow<List<User>> = _customers

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    init {
        observeCustomers()
    }

    // 1. Lắng nghe Realtime danh sách Khách hàng từ Firestore
    private fun observeCustomers() {
        firestore.collection("users")
            // .whereEqualTo("role", "USER") // Bật lên nếu DB của bạn có phân biệt trường role
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminCustomer", "Lỗi tải dữ liệu: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<User>()
                    for (doc in snapshot.documents) {
                        val user = doc.toObject(User::class.java)?.copy(uid = doc.id)
                        if (user != null) {
                            list.add(user)
                        }
                    }
                    _customers.value = list
                    _totalCount.value = list.size
                }
            }
    }

    // 2. Cập nhật query tìm kiếm
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // 3. THÊM tài khoản khách hàng mới
    // 1. Hàm Thêm mới tài khoản với đầy đủ các trường dữ liệu tùy chọn
    fun addCustomer(name: String, phone: String, email: String, address: String, role: String, vehicleName: String) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        // Tạo tài khoản Auth đồng bộ mật khẩu mặc định là Số điện thoại
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
                    "avatarUrl" to ""
                )
                firestore.collection("users").document(uid).set(newCustomer)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("AdminCustomer", "Lỗi tạo Auth: ${e.message}")
            }
    }

    // 2. Hàm Cập nhật thông tin tài khoản
    fun updateCustomer(uid: String, name: String, phone: String, email: String, address: String, role: String, vehicleName: String) {
        val updates = mapOf(
            "name" to name,
            "phone" to phone,
            "email" to email,
            "address" to address,
            "role" to role,
            "vehicleName" to vehicleName
        )
        firestore.collection("users").document(uid).update(updates)
    }

    // 5. XÓA tài khoản khách hàng
    fun deleteCustomer(uid: String) {
        firestore.collection("users").document(uid).delete()
    }

    // 6. THAY ĐỔI TRẠNG THÁI KHÓA/MỞ KHÓA
    fun toggleLockStatus(uid: String, currentAvailable: Boolean) {
        // Đảo ngược trạng thái hoạt động: nếu đang true thì đổi thành false và ngược lại
        val nextAvailable = !currentAvailable

        firestore.collection("users")
            .document(uid)
            .update("isAvailable", nextAvailable)
            .addOnSuccessListener {
                Log.d("AdminCustomer", "Cập nhật trạng thái khóa thành công!")
            }
            .addOnFailureListener { e ->
                Log.e("AdminCustomer", "Lỗi khi cập nhật trạng thái: ${e.message}")
            }
    }
}