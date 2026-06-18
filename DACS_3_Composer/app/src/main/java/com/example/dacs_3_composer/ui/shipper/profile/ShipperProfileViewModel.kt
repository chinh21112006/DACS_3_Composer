package com.example.dacs_3_composer.ui.shipper.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.User
import com.example.dacs_3_composer.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ShipperProfileViewModel : ViewModel() {

    private val repository = UserRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _totalOrders = MutableStateFlow(0)
    val totalOrders: StateFlow<Int> = _totalOrders

    private val _totalEarnings = MutableStateFlow(0.0)
    val totalEarnings: StateFlow<Double> = _totalEarnings

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadProfile()
        observeShipperStats()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _user.value = repository.getCurrentUser()
            } catch (e: Exception) {
                Log.e("Profile", "Lỗi tải thông tin: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeShipperStats() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("orders")
            .whereEqualTo("shipperId", uid)
            .whereEqualTo("status", "COMPLETED")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Stats", "Lỗi Firebase: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var totalShippingFeeSum = 0.0
                    val documentCount = snapshot.size()

                    // Duyệt qua từng đơn hàng tìm thấy
                    for (document in snapshot.documents) {
                        // Lấy duy nhất ô shippingFee viết thường từ DB
                        val feeRaw = document.get("shippingFee")
                        val fee = feeRaw?.toString()?.toDoubleOrNull() ?: 0.0

                        // Cộng dồn các ô shippingFee lại với nhau
                        totalShippingFeeSum += fee

                        // IN RA LOGCAT ĐỂ CHỨNG MINH THỰC TẾ
                        Log.d("KIEM_TRA_TIEN", "Đơn ID: ${document.id} | Có tiền shippingFee = $fee")
                    }

                    // Cập nhật lên giao diện
                    _totalOrders.value = documentCount
                    _totalEarnings.value = totalShippingFeeSum

                    Log.d("KIEM_TRA_TIEN", "==> TỔNG CỘNG: Tìm thấy $documentCount đơn | Tổng tiền ship cộng lại = $totalShippingFeeSum")
                }
            }
    }

    fun updateProfile(
        name: String,
        phone: String,
        address: String,
        vehicleName: String,
        imageFile: File?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                var avatarUrl = _user.value.avatarUrl

                if (imageFile != null) {
                    avatarUrl = repository.uploadAvatar(imageFile)
                }

                val updatedUser = _user.value.copy(
                    name = name,
                    phone = phone,
                    address = address,
                    vehicleName = vehicleName,
                    avatarUrl = avatarUrl
                )

                repository.updateProfile(updatedUser)
                _user.value = updatedUser
                onDone()
            } catch (e: Exception) {
                Log.e("UpdateProfile", "Lỗi lưu dữ liệu: ${e.message}")
                _errorMessage.value = e.localizedMessage ?: "Cập nhật thất bại, vui lòng kiểm tra lại!"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Gọi lệnh đăng xuất của Firebase Auth
                auth.signOut()

                // 2. Xóa sạch dữ liệu User trong StateFlow về mặc định
                _user.value = User()
                _totalOrders.value = 0
                _totalEarnings.value = 0.0

                // 3. Kích hoạt callback để chuyển màn hình trên UI
                onSuccess()
            } catch (e: Exception) {
                Log.e("Logout", "Lỗi khi đăng xuất: ${e.message}")
                _errorMessage.value = "Không thể đăng xuất, vui lòng thử lại!"
            }
        }
    }
}