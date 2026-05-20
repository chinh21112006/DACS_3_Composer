package com.example.dacs_3_composer.ui.restaurant.menu

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.R
import com.example.dacs_3_composer.data.model.Dish
import com.example.dacs_3_composer.ui.restaurant.menu.components.*

@Composable
fun RestaurantMenuScreen(
    modifier: Modifier = Modifier,
    viewModel: RestaurantViewModel = viewModel()
) {
    val dishes by viewModel.dishes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryTab by remember { mutableStateOf("Tất cả") }

    // Trạng thái cho Dialog thêm/sửa món
    var showAddDialog by remember { mutableStateOf(false) }
//    Khai báo để theo dõi
    var editingDish by remember { mutableStateOf<Dish?>(null) }

    // Xử lý thông báo lỗi
    LaunchedEffect(error) {
        error?.let {
            // Có thể hiển thị Toast ở đây
            viewModel.clearError()
        }
    }

    val filteredDishes = remember(searchQuery, selectedCategoryTab, dishes) {
        dishes.filter { dish ->
            val matchesSearch = dish.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryTab == "Tất cả" || dish.category == selectedCategoryTab
            matchesSearch && matchesCategory
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                Header(
                    avatarRes = R.drawable.banner1,
                    onNotificationClick = { /* TODO: Handle notification click */ }
                )
            }
            item { ScreenTitle(title = "Quản lý thực đơn", modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
            item { SearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
            item { CategoryTabs(selectedCategory = selectedCategoryTab, onCategorySelected = { selectedCategoryTab = it }, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) }

            if (isLoading && dishes.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }
//      Lấy món ăn hiển thị lên màn hình và có nút sửa xóa
//            Ở nút sửa thì đưa dữ liệu vào và cho dish vào để phân biệt thêm và sửa
            items(items = filteredDishes, key = { it.id }) { dish ->
                RestaurantDishItem(
                    dishName = dish.name,
                    restaurantName = "Của bạn",
                    price = "${dish.price}đ",
                    isAvailable = dish.available,
                    imageUrl = dish.imageUrl,
                    onEditClick = { editingDish = dish; showAddDialog = true },
                    onDeleteClick = { viewModel.deleteDish(dish.id) },
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                    // BỔ SUNG DÒNG NÀY ĐỂ FIX LỖI BÁO ĐỎ:
                    onToggleAvailability = { isChecked ->
                        viewModel.toggleDishAvailability(dish)
                    }
                )
            }
        }
//  Thêm sản phẩm mới
        FloatingActionButton(
            onClick = { editingDish = null; showAddDialog = true },
            containerColor = Color(0xFF2159BC),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 20.dp)
        ) {
            Icon(Icons.Default.Add, "Thêm món", modifier = Modifier.size(28.dp))
        }
// Kiểm tra xem nếu null thì nút thêm còn có thì sửa
        if (showAddDialog) {
            DishDialog(
//                Như ở trên null hoặc có dữ liệu, phân biệt thêm và sửa
                dish = editingDish,
//                Thoát ra nếu không muốn
                onDismiss = { showAddDialog = false },
//                Đưa dữ liệu nhập liệu vào
                onConfirm = { name, price, cat, desc, uri ->
//                    Nếu thêm thì gọi viewmodel và ngược lại thì sửa
                    if (editingDish == null) {
                        viewModel.addDish(name, price, cat, desc, uri)
                    } else {
                        viewModel.updateDish(editingDish!!.copy(name = name, price = price, category = cat, description = desc), uri)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}
//    Dialog thêm sửa món ăn giao diện
@Composable
fun DishDialog(
    dish: Dish?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(dish?.name ?: "") }
    var price by remember { mutableStateOf(dish?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(dish?.category ?: "Món chính") }
    var description by remember { mutableStateOf(dish?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (dish == null) "Thêm món mới" else "Sửa món ăn") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Thêm ô nhập danh mục để chủ quán tự điền thay vì bị cố định "Món chính"
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Danh mục món ăn") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Giá (VNĐ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (imageUri != null) "Đã chọn ảnh" else "Chọn ảnh món ăn")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(name, price.toDoubleOrNull() ?: 0.0, category, description, imageUri)
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}