package com.example.dacs_3_composer.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<String>("")
    val authState: StateFlow<String> = _authState

    private val webClientId = "963036038937-oc3omdisdodgmdfoqn24ko5keeat5tsi.apps.googleusercontent.com"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ==========================================
    // HÀM XỬ LÝ ĐĂNG NHẬP BẰNG GOOGLE
    // ==========================================
    fun loginWithGoogle(context: Context) {
        _authState.value = "Đang kết nối với Google..."
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    val googleIdToken = credential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                val email = auth.currentUser?.email ?: ""

                                if (uid != null) {
                                    db.collection("users").document(uid).get()
                                        .addOnSuccessListener { document ->
                                            if (document != null && document.exists()) {
                                                val role = document.getString("role") ?: "user"
                                                triggerLoginSuccessByRole(role)
                                            } else {
                                                val userMap = hashMapOf(
                                                    "email" to email,
                                                    "role" to "user"
                                                )
                                                db.collection("users").document(uid).set(userMap)
                                                    .addOnSuccessListener {
                                                        // 🌟 ĐÃ SỬA: Gọi hàm tập trung để sinh chuỗi khớp với MainActivity
                                                        triggerLoginSuccessByRole("user")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        _authState.value = "Lỗi tạo thông tin dữ liệu Google: ${e.message}"
                                                    }
                                            }
                                        }
                                }
                            } else {
                                _authState.value = "Lỗi Firebase: ${task.exception?.message}"
                            }
                        }
                }
            } catch (e: Exception) {
                _authState.value = "Đã hủy đăng nhập Google"
            }
        }
    }

    // ==========================================
    // HÀM XỬ LÝ ĐĂNG KÝ BẰNG EMAIL/PASSWORD
    // ==========================================
    fun registerUser(email: String, password: String, confirmPass: String) {
        if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            _authState.value = "Vui lòng nhập đầy đủ thông tin!"
            return
        }
        if (password != confirmPass) {
            _authState.value = "Mật khẩu nhập lại không khớp!"
            return
        }

        _authState.value = "Đợi xí, đang xử lý..."
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null){
                        val userMap = hashMapOf(
                            "email" to email,
                            "role" to "user" // 🌟 Mặc định đăng ký tài khoản tự do là khách mua hàng ("user")
                        )
                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                _authState.value = "Đăng ký thành công!"
                            }.addOnFailureListener { e->
                                _authState.value = "Tạo tài khoản thành công nhưng lỗi database: ${e.message}."
                            }
                    }
                } else {
                    _authState.value = "Lỗi: ${task.exception?.message}"
                }
            }
    }

    // ==========================================
    // HÀM XỬ LÝ ĐĂNG NHẬP BẰNG EMAIL/PASSWORD
    // ==========================================
    fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = "Vui lòng không để trống Email/Password!"
            return
        }

        _authState.value = "Loading..."
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val role = document.getString("role") ?: "user"
                                    triggerLoginSuccessByRole(role)
                                } else {
                                    // 🌟 ĐÃ SỬA: Đồng bộ chuỗi đăng nhập User thành công
                                    triggerLoginSuccessByRole("user")
                                }
                            }
                            .addOnFailureListener { e ->
                                _authState.value = "Lỗi kết nối phân quyền: ${e.message}"
                            }
                    }
                } else {
                    _authState.value = "Sai tài khoản hoặc mật khẩu!"
                }
            }
    }

    // 🌟 HÀM PHỤ TRỢ: Phát tín hiệu authState cụ thể theo từng Role để bên ngoài giao diện (UI) bắt chuỗi ký tự nhảy màn hình
    private fun triggerLoginSuccessByRole(role: String) {
        when (role) {
            "admin" -> _authState.value = "Đăng nhập Admin thành công!"
            "restaurant" -> _authState.value = "Đăng nhập Restaurant thành công!"
            "shipper" -> _authState.value = "Đăng nhập Shipper thành công!"
            else -> _authState.value = "Đăng nhập User thành công!"
        }
    }

    // HÀM XỬ LÝ ĐĂNG XUẤT
    fun logoutUser() {
        auth.signOut()
        _authState.value = "Đã đăng xuất!"
    }

    fun clearAuthState() {
        _authState.value = ""
    }
}