package com.example.dacs_3_composer.ui.restaurant.store

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dacs_3_composer.data.model.DishItem
import com.example.dacs_3_composer.ui.restaurant.store.components.DishRowManageItem
import com.example.dacs_3_composer.ui.restaurant.store.components.RestaurantInfoManageCard

@Composable
fun RestaurantManageScreen(
    restaurantId: String, // ID của quán ăn cần quản lý
    onBackClick: () -> Unit,
    onEditCoverClick: () -> Unit,
    onEditDishClick: (DishItem) -> Unit,
    viewModel: StoreViewModel = viewModel()
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            // Gửi uri ảnh cục bộ trong máy này qua ViewModel để thảy lên Cloudinary
            viewModel.uploadCoverToCloudinary(
                imageUri = selectedImageUri,
                restaurantId = restaurantId,
                uploadPreset = "ml_default",
                onSuccess = {
                    Log.d("UI", "Đã cập nhật xong ảnh bìa lấy từ Cloudinary!")
                }
            )
        }
    }
    // Tự động tải dữ liệu thật từ Firebase về khi vừa mở màn hình
    LaunchedEffect(restaurantId) {
        viewModel.fetchStoreData(restaurantId)
    }

    val restaurant = viewModel.restaurantDetail.value
    val dishes = viewModel.dishesList.value

    // Các trạng thái để quản lý việc Ẩn/Hiện Dialog chỉnh sửa thông tin
    var showDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }

    var showCoverDialog by remember { mutableStateOf(false) }
    var editCoverUrl by remember { mutableStateOf("") }

// Cập nhật giá trị link ảnh mặc định khi dữ liệu quán load xong
    LaunchedEffect(restaurant) {
        restaurant?.let {
            editName = it.name
            editDescription = it.description
            editAddress = it.address
            editCoverUrl = it.coverImage // Thêm dòng này
        }
    }

    Scaffold { innerPadding ->
        if (restaurant == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1E56A0))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF8F9FA))
            ) {
                // Phần 1: Ảnh bìa
                item {
                    Box(modifier = Modifier.height(220.dp).fillMaxWidth()) {
                        AsyncImage(
                            model = restaurant.coverImage,
                            contentDescription = "Ảnh bìa nhà hàng",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }

                        SmallFloatingActionButton(
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            containerColor = Color.White,
                            contentColor = Color(0xFF1E56A0),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa ảnh bìa", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Phần 2: Khối thông tin chi tiết Nhà hàng + Khi bấm nút sửa sẽ MỞ DIALOG
                item {
                    RestaurantInfoManageCard(
                        restaurant = restaurant,
                        onEditInfoClick = { showDialog = true } // Bật hộp thoại nhập liệu lên
                    )
                }

                // Phần 3: Tiêu đề danh sách món
                item {
                    Text(
                        text = "Danh sách món ăn hiện tại",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                    )
                }

                // Phần 4: Danh sách món ăn
                items(dishes) { dish ->
//                    Để sản phẩm vào và vẽ ra kiểu để hiển thị món ăn
                    DishRowManageItem(
                        dish = dish,
                        onItemClick = { onEditDishClick(dish) }
                    )
                }
            }
        }

        // 🌟 HỘP THOẠI (DIALOG) CHỈNH SỬA THÔNG TIN CỦA CỬA HÀNG
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Chỉnh sửa thông tin quán", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Tên nhà hàng") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Mô tả quán") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text("Địa chỉ") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E56A0)),
                        onClick = {
                            // Gọi ViewModel đẩy thông tin mới lên Firebase Firestore
                            viewModel.updateRestaurantInfo(
                                restaurantId = restaurantId,
                                newName = editName,
                                newDescription = editDescription,
                                newAddress = editAddress,
                                onSuccess = { showDialog = false } // Lưu thành công thì đóng Dialog
                            )
                        }
                    ) {
                        Text("Lưu", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Hủy", color = Color.Gray)
                    }
                }
            )
        }
        // 🌟 HỘP THOẠI (DIALOG) CHỈNH SỬA ẢNH BÌA CỦA CỬA HÀNG
        if (showCoverDialog) {
            AlertDialog(
                onDismissRequest = { showCoverDialog = false },
                title = { Text(text = "Thay đổi ảnh bìa quán", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Nhập link URL ảnh mới từ internet để thay đổi giao diện hiển thị của quán.", fontSize = 14.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = editCoverUrl,
                            onValueChange = { editCoverUrl = it },
                            label = { Text("Đường dẫn link ảnh (URL)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E56A0)),
                        onClick = {
                            // Gọi ViewModel đẩy link ảnh mới lên Firebase
                            viewModel.updateCoverImage(
                                restaurantId = restaurantId,
                                newCoverUrl = editCoverUrl,
                                onSuccess = { showCoverDialog = false } // Thành công thì đóng Dialog
                            )
                        }
                    ) {
                        Text("Lưu ảnh", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCoverDialog = false }) {
                        Text("Hủy", color = Color.Gray)
                    }
                }
            )
        }
    }
}