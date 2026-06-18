//package com.example.dacs_3_composer.data.firebase
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.example.dacs_3_composer.MainActivity
//import com.example.dacs_3_composer.R
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//
//class ChatMessagingService : FirebaseMessagingService() {
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        updateTokenToFirestore(token)
//    }
//
//    private fun updateTokenToFirestore(token: String) {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        FirebaseFirestore.getInstance().collection("users").document(uid)
//            .update("fcmToken", token)
//    }
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//
//        val title = remoteMessage.notification?.title ?: "Tin nhắn mới"
//        val body = remoteMessage.notification?.body ?: ""
//        val convId = remoteMessage.data["conversationId"]
//
//        sendNotification(title, body, convId)
//    }
//
//    private fun sendNotification(title: String, body: String, convId: String?) {
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            putExtra("conversationId", convId)
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val channelId = "chat_notifications"
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId, "Chat Messages",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.mipmap.ic_launcher) // Dùng ic_launcher mặc định
//            .setContentTitle(title)
//            .setContentText(body)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
//    }
//}
