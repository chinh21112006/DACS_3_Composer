package com.example.dacs_3_composer.ui.admin.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.Promotion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminPromotionViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "AdminPromotionVM"

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        fetchPromotions()
    }

    private fun fetchPromotions() {
        _isLoading.value = true
        firestore.collection("promotions")
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    Log.e(TAG, "Error fetching promotions: ${error.message}", error)
                    // Fallback if index is missing
                    if (error.message?.contains("index") == true) {
                        fetchPromotionsNoSort()
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Promotion::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.e(TAG, "Parse error for ${doc.id}", e)
                                null
                            }
                        }
                        _promotions.value = list
                    } catch (e: Exception) {
                        Log.e(TAG, "Processing error", e)
                    }
                }
            }
    }

    private fun fetchPromotionsNoSort() {
        firestore.collection("promotions")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Promotion::class.java)?.copy(id = doc.id)
                    }
                    // Sort in memory instead
                    _promotions.value = list.sortedByDescending { it.startDate }
                }
            }
    }

    fun addPromotion(promotion: Promotion) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Convert to map for safe write
                val data = hashMapOf(
                    "code" to promotion.code,
                    "title" to promotion.title,
                    "type" to promotion.type,
                    "value" to promotion.value,
                    "maxDiscount" to promotion.maxDiscount,
                    "minOrderValue" to promotion.minOrderValue,
                    "usageCount" to 0L,
                    "usageLimit" to promotion.usageLimit,
                    "startDate" to promotion.startDate,
                    "endDate" to promotion.endDate,
                    "status" to promotion.status,
                    "description" to promotion.description
                )
                // Đảm bảo tên collection là "promotions" hoặc "Promotions" tùy theo Firebase của bạn
                firestore.collection("promotions").add(data).await()
                _toastMessage.emit("Thêm mã khuyến mãi thành công!")
                Log.d(TAG, "Promotion added successfully")
            } catch (e: Exception) {
                _toastMessage.emit("Lỗi: ${e.message}")
                Log.e(TAG, "Error adding promotion: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePromotion(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            try {
                firestore.collection("promotions").document(id).delete().await()
                _toastMessage.emit("Đã xóa mã khuyến mãi")
            } catch (e: Exception) {
                _toastMessage.emit("Lỗi khi xóa: ${e.message}")
                Log.e(TAG, "Error deleting promotion", e)
            }
        }
    }

    fun toggleStatus(id: String, currentStatus: String) {
        if (id.isBlank()) return
        val newStatus = if (currentStatus == "active") "expired" else "active"
        viewModelScope.launch {
            try {
                firestore.collection("promotions").document(id).update("status", newStatus).await()
                _toastMessage.emit("Đã cập nhật trạng thái")
            } catch (e: Exception) {
                _toastMessage.emit("Lỗi cập nhật: ${e.message}")
                Log.e(TAG, "Error toggling status", e)
            }
        }
    }
}
