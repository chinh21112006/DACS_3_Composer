package com.example.dacs_3_composer.ui.shipper.dashboard.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.ui.shipper.dashboard.ShipperViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@SuppressLint("MissingPermission", "ClickableViewAccessibility")
@Composable
fun ShipperMapView(
    order: Order,
    modifier: Modifier = Modifier.fillMaxWidth().height(320.dp),
    shipperViewModel: ShipperViewModel = viewModel()
) {
    val context = LocalContext.current
    val deviceLocation by shipperViewModel.currentShipperLocation.collectAsState()
    val mapContext = remember(context) { context.findActivity() ?: context }

    // Khởi tạo cấu hình OSMDroid một lần duy nhất
    LaunchedEffect(context) {
        try {
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Biến trạng thái lưu tọa độ thực tế của Shipper lấy từ Firebase
    var firebaseShipperPoint by remember { mutableStateOf<GeoPoint?>(null) }

    val trackingRef = remember(order.id) {
        Firebase.database.getReference("tracking").child(order.id)
    }

    // 🌟 GIẢI PHÁP: Lắng nghe Firebase bằng các hiệu ứng vòng đời chuẩn của Compose (Lớp kiến trúc dữ liệu riêng biệt)
    DisposableEffect(order.id) {
        var listener: ValueEventListener? = null

        if (order.id.isNotBlank() && order.id != "HEATING_MAP_PREVIEW") {
            listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val lat = (snapshot.child("lat").value as? Number)?.toDouble() ?: return
                        val lng = (snapshot.child("lng").value as? Number)?.toDouble() ?: return
                        firebaseShipperPoint = GeoPoint(lat, lng)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            trackingRef.addValueEventListener(listener)
        }

        onDispose {
            listener?.let { trackingRef.removeEventListener(it) }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val actualCtx = ctx.findActivity() ?: ctx
            MapView(actualCtx).apply {
                try {
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    controller.setZoom(16.0)

                    setOnTouchListener { view, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> view.parent.requestDisallowInterceptTouchEvent(true)
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.parent.requestDisallowInterceptTouchEvent(false)
                        }
                        false
                    }

                    // 🌟 KHỞI TẠO CÁC OVERLAYS TẠI ĐÂY: Truyền trực tiếp `this` (MapView thực tế) thay vì `null`
                    val destinationMarker = Marker(this).apply {
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        id = "destination"
                    }
                    val shipperMarker = Marker(this).apply {
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "Vị trí của bạn"
                        id = "shipper"
                        try {
                            icon = actualCtx.resources.getDrawable(org.osmdroid.library.R.drawable.person, null).apply {
                                setTint(android.graphics.Color.parseColor("#2563EB"))
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    val routePolyline = Polyline(this).apply {
                        id = "route"
                        color = android.graphics.Color.parseColor("#2563EB")
                        width = 8f
                    }

                    overlays.add(destinationMarker)
                    overlays.add(shipperMarker)
                    overlays.add(routePolyline)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        update = { mapView ->
            try {
                // Lấy lại các Overlays đã khởi tạo trong phần factory dựa theo ID hoặc thứ tự để cập nhật dữ liệu mới
                val destinationMarker = mapView.overlays.firstOrNull { (it as? Marker)?.id == "destination" } as? Marker
                val shipperMarker = mapView.overlays.firstOrNull { (it as? Marker)?.id == "shipper" } as? Marker
                val routePolyline = mapView.overlays.firstOrNull { (it as? Polyline)?.id == "route" } as? Polyline

                val isGoingToRestaurant = order.status == "ACCEPTED"
                val destLat = if (isGoingToRestaurant) order.restaurantLat else order.customerLat
                val destLng = if (isGoingToRestaurant) order.restaurantLng else order.customerLng

                val defaultLat = deviceLocation?.get("lat") ?: 15.9733
                val defaultLng = deviceLocation?.get("lng") ?: 108.2517

                val finalLat = destLat ?: defaultLat
                val finalLng = destLng ?: defaultLng
                val destinationPoint = GeoPoint(finalLat, finalLng)

                // 1. Cập nhật Marker điểm đến
                destinationMarker?.apply {
                    position = destinationPoint
                    if (order.id == "HEATING_MAP_PREVIEW") {
                        title = "Điểm giám sát hệ thống"
                        try {
                            icon = mapView.context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).apply {
                                setTint(android.graphics.Color.parseColor("#94A3B8"))
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    } else {
                        if (isGoingToRestaurant) {
                            title = order.restaurantName.ifBlank { "Nhà hàng" }
                            try {
                                icon = mapView.context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).apply {
                                    setTint(android.graphics.Color.parseColor("#2ECC71"))
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        } else {
                            title = order.customerName.ifBlank { "Khách hàng" }
                            try {
                                icon = mapView.context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).apply {
                                    setTint(android.graphics.Color.parseColor("#E74C3C"))
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                }

                // 2. Xác định tọa độ hiển thị của Shipper (Ưu tiên Firebase, nếu không có/Bản đồ nhiệt thì lấy GPS thiết bị)
                val shipperPoint = if (order.id != "HEATING_MAP_PREVIEW" && order.id.isNotBlank()) {
                    firebaseShipperPoint
                } else {
                    GeoPoint(defaultLat, defaultLng)
                }

                // 3. Cập nhật vị trí Shipper & Đường nối (Polyline)
                if (shipperPoint != null) {
                    shipperMarker?.position = shipperPoint
                    routePolyline?.setPoints(listOf(shipperPoint, destinationPoint))

                    // Di chuyển camera theo vị trí shipper nếu có cập nhật mới từ Firebase
                    if (order.id != "HEATING_MAP_PREVIEW") {
                        mapView.controller.animateTo(shipperPoint)
                    } else {
                        mapView.controller.setCenter(shipperPoint)
                    }
                }

                mapView.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        onRelease = { mapView ->
            try {
                mapView.onDetach()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
}