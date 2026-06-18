package com.example.dacs_3_composer.ui.admin.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class ProfileUiState(
    val uid: String = "",
    val adminName: String = "",
    val adminEmail: String = "",
    val adminPhone: String = "",
    val avatarUrl: String = "",
    val totalUsersCount: String = "0",
    val systemStatus: String = "99.9%"
)

class AdminProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val cloudinaryCloudName = "dhscw17vq"
    private val cloudinaryUploadPreset = "ml_default"

    init {
        loadAdminProfileAndStats()
    }

    private fun loadAdminProfileAndStats() {
        val currentUid = auth.currentUser?.uid ?: return

        // 1. Lắng nghe thông tin tài khoản đúng chuẩn của Admin đang đăng nhập hiện tại
        firestore.collection("users").document(currentUid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e("AdminProfileVM", "Lỗi lắng nghe dữ liệu user", error)
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val adminUser = documentSnapshot.toObject(User::class.java)
                    if (adminUser != null) {
                        // ✅ KHÔNG dùng chuỗi cứng của người khác, ưu tiên lấy trực tiếp từ Firebase Auth nếu trên Firestore bị trống
                        _uiState.value = _uiState.value.copy(
                            uid = currentUid,
                            adminName = adminUser.name.ifBlank { auth.currentUser?.displayName ?: "Admin" },
                            adminEmail = adminUser.email.ifBlank { auth.currentUser?.email ?: "" },
                            adminPhone = adminUser.phone ?: "",
                            avatarUrl = adminUser.avatarUrl ?: ""
                        )
                    }
                }
            }

        // 2. Realtime đếm tổng thành viên và định dạng rút gọn (ví dụ: 24.5k)
        firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val count = snapshot.size()
                    val formattedCount = if (count >= 1000) {
                        String.format("%.1fk", count / 1000.0)
                    } else {
                        count.toString()
                    }
                    _uiState.value = _uiState.value.copy(
                        totalUsersCount = formattedCount
                    )
                }
            }
    }

    // 🌐 Logic Upload ảnh lên Cloudinary
    fun uploadAvatarToCloudinary(context: Context, fileUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
                val bytes = inputStream?.readBytes()
                if (bytes == null) {
                    withContext(Dispatchers.Main) { onFailure(Exception("Không thể đọc file ảnh")) }
                    return@launch
                }

                val url = URL("https://api.cloudinary.com/v1_1/$cloudinaryCloudName/image/upload")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val outputStream = connection.outputStream
                val base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                val data = "file=" + java.net.URLEncoder.encode("data:image/jpeg;base64,$base64Image", "UTF-8") +
                        "&upload_preset=" + java.net.URLEncoder.encode(cloudinaryUploadPreset, "UTF-8")

                outputStream.write(data.toByteArray())
                outputStream.flush()
                outputStream.close()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(responseText)
                    val secureUrl = jsonObject.getString("secure_url")

                    firestore.collection("users").document(_uiState.value.uid)
                        .update("avatarUrl", secureUrl)
                        .addOnSuccessListener {
                            viewModelScope.launch(Dispatchers.Main) { onSuccess(secureUrl) }
                        }
                        .addOnFailureListener { e ->
                            viewModelScope.launch(Dispatchers.Main) { onFailure(e) }
                        }
                } else {
                    withContext(Dispatchers.Main) { onFailure(Exception("Cloudinary phản hồi mã lỗi: ${connection.responseCode}")) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onFailure(e) }
            }
        }
    }

    // Hàm cập nhật thông tin cá nhân
    fun updateAccountInfo(newName: String, newEmail: String, newPhone: String, onComplete: (Boolean) -> Unit) {
        val dataMap = mapOf(
            "name" to newName,
            "email" to newEmail,
            "phone" to newPhone
        )
        firestore.collection("users").document(_uiState.value.uid)
            .update(dataMap)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    // 🎯 THÊM MỚI: Xử lý logic đăng xuất sạch dữ liệu giống cấu trúc của Shipper bên bạn
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                auth.signOut()
                // Reset State về rỗng hoàn toàn để tránh lưu bộ nhớ đệm của Admin cũ
                _uiState.value = ProfileUiState()
                onSuccess()
            } catch (e: Exception) {
                Log.e("LogoutAdmin", "Lỗi đăng xuất: ${e.message}")
            }
        }
    }
}