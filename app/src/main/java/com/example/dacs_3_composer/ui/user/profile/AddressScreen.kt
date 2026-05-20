package com.example.dacs_3_composer.ui.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.data.model.UserAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddressViewModel = viewModel()
) {
    val addresses by viewModel.addressList.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<UserAddress?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text("Địa chỉ đã lưu", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingAddress = null
                    showDialog = true
                },
                containerColor = Color(0xFF2159BC),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm mới", fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2159BC))
            } else if (addresses.isEmpty()) {
                Text(
                    text = "Bạn chưa lưu địa chỉ nào.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(addresses) { address ->
                        AddressItemCard(
                            address = address,
                            onEditClick = {
                                editingAddress = address
                                showDialog = true
                            },
                            onDeleteClick = {
                                viewModel.deleteAddress(address)
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddressFormDialog(
                addressToEdit = editingAddress,
                onDismiss = { showDialog = false },
                onSave = { updatedAddress ->
                    if (editingAddress == null) {
                        // Thêm mới vào mảng
                        viewModel.addAddress(updatedAddress) { showDialog = false }
                    } else {
                        // Sửa phần tử trong mảng
                        viewModel.updateAddress(editingAddress!!, updatedAddress) { showDialog = false }
                    }
                }
            )
        }
    }
}

@Composable
fun AddressItemCard(
    address: UserAddress,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2159BC))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (address.name.isNotBlank()) address.name else "Địa chỉ khách",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (address.phone.isNotBlank()) {
                    Text(text = "SĐT: ${address.phone}", fontSize = 14.sp, color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(text = address.address, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color(0xFF2159BC),
                    modifier = Modifier.size(20.dp).clickable { onEditClick() }
                )
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color(0xFFC0392B),
                    modifier = Modifier.size(20.dp).clickable { onDeleteClick() }
                )
            }
        }
    }
}

@Composable
fun AddressFormDialog(
    addressToEdit: UserAddress?,
    onDismiss: () -> Unit,
    onSave: (UserAddress) -> Unit
) {
    var name by remember { mutableStateOf(addressToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(addressToEdit?.phone ?: "") }
    var addressStr by remember { mutableStateOf(addressToEdit?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = { Text(if (addressToEdit == null) "Thêm địa chỉ mới" else "Sửa địa chỉ", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên người nhận (Có thể để trống)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = addressStr, onValueChange = { addressStr = it }, label = { Text("Địa chỉ (Ví dụ: 93 Nguyễn Đình Chiểu)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), maxLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isNotBlank() && addressStr.isNotBlank()) {
                        onSave(
                            UserAddress(
                                name = name,
                                phone = phone,
                                address = addressStr
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2159BC))
            ) {
                Text("Lưu lại")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy bỏ", color = Color.Gray) }
        }
    )
}