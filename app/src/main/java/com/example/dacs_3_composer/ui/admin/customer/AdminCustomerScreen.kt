package com.example.dacs_3_composer.ui.admin.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.data.model.User
import com.example.dacs_3_composer.ui.admin.customer.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCustomerScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminCustomerViewModel = viewModel()
) {
    val customers by viewModel.customers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedUserForEdit by remember { mutableStateOf<User?>(null) }

    // Các biến State để lưu giá trị nhập liệu trong Dialog
    var inputName by remember { mutableStateOf("") }
    var inputPhone by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputAddress by remember { mutableStateOf("") }
    var inputVehicleName by remember { mutableStateOf("") }

    // State cho việc chọn Quyền/Vai trò (Dropdown Menu)
    var inputRole by remember { mutableStateOf("user") }
    var expandedDropdown by remember { mutableStateOf(false) }
    val roleOptions = listOf("admin", "restaurant", "user", "shipper")

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            CustomerTopBar(
                onAddUserClick = {
                    selectedUserForEdit = null
                    inputName = ""
                    inputPhone = ""
                    inputEmail = ""
                    inputAddress = ""
                    inputVehicleName = ""
                    inputRole = "user"
                    showDialog = true
                }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                CustomerSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) }
                )
            }

            item {
                CustomerStatsRow(
                    totalCustomers = "$totalCount người",
                    newThisMonth = "+${filteredCustomers.size} tìm thấy"
                )
            }

            items(filteredCustomers) { customer ->
                CustomerCardItem(
                    name = customer.name.ifBlank { "Chưa đặt tên" },
                    phone = customer.phone.ifBlank { "Không có SĐT" },
                    email = customer.email.ifBlank { "Chưa có Email" },
                    role = customer.role.ifBlank { "user" },
                    avatarUrl = customer.avatarUrl,
                    isAvailable = customer.isAvailable,
                    onEditClick = {
                        selectedUserForEdit = customer
                        inputName = customer.name
                        inputPhone = customer.phone
                        inputEmail = customer.email
                        inputAddress = customer.address
                        inputVehicleName = customer.vehicleName
                        inputRole = customer.role.ifBlank { "user" }
                        showDialog = true
                    },
                    onDeleteClick = {
                        viewModel.deleteCustomer(customer.uid)
                    },
                    onLockToggle = {
                        viewModel.toggleLockStatus(customer.uid, customer.isAvailable)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // HỘP THOẠI (DIALOG) THÊM & SỬA ĐẦY ĐỦ THÔNG TIN
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        text = if (selectedUserForEdit == null) "Thêm Tài Khoản Mới" else "Sửa Thông Tin",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Họ và tên") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputPhone,
                            onValueChange = { inputPhone = it },
                            label = { Text("Số điện thoại") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("Địa chỉ Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputAddress,
                            onValueChange = { inputAddress = it },
                            label = { Text("Địa chỉ cư trú") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 🎯 THÀNH PHẦN CHỌN VAI TRÒ (DROPDOWN MENU)
                        ExposedDropdownMenuBox(
                            expanded = expandedDropdown,
                            onExpandedChange = { expandedDropdown = !expandedDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = inputRole.uppercase(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vai trò (Role)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                roleOptions.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role.uppercase()) },
                                        onClick = {
                                            inputRole = role
                                            expandedDropdown = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        // 🎯 CHỈ HIỂN THỊ TRƯỜNG NHẬP TÊN XE NẾU CHỌN ROLE LÀ SHIPPER
                        if (inputRole == "shipper") {
                            OutlinedTextField(
                                value = inputVehicleName,
                                onValueChange = { inputVehicleName = it },
                                label = { Text("Tên phương tiện di chuyển") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                        onClick = {
                            if (inputName.isNotBlank() && inputPhone.isNotBlank() && inputEmail.isNotBlank()) {
                                val currentUser = selectedUserForEdit
                                if (currentUser == null) {
                                    // Gọi hàm thêm mới trong ViewModel (Tự tạo mật khẩu bằng SĐT hoặc xử lý sau)
                                    viewModel.addCustomer(
                                        name = inputName,
                                        phone = inputPhone,
                                        email = inputEmail,
                                        address = inputAddress,
                                        role = inputRole,
                                        vehicleName = if (inputRole == "shipper") inputVehicleName else ""
                                    )
                                } else {
                                    // Gọi hàm sửa thông tin
                                    viewModel.updateCustomer(
                                        uid = currentUser.uid,
                                        name = inputName,
                                        phone = inputPhone,
                                        email = inputEmail,
                                        address = inputAddress,
                                        role = inputRole,
                                        vehicleName = if (inputRole == "shipper") inputVehicleName else ""
                                    )
                                }
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Lưu Lại")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Hủy", color = Color.Gray)
                    }
                }
            )
        }
    }
}