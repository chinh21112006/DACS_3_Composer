package com.example.dacs_3_composer.data.repository

import android.util.Log
import com.example.dacs_3_composer.data.model.ChatMessage
import com.example.dacs_3_composer.data.model.Conversation
import com.example.dacs_3_composer.data.model.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUid get() = auth.currentUser?.uid ?: ""

    /**
     * Lấy danh sách hội thoại. 
     * Đối với ADMIN: Lấy tin cá nhân + TOÀN BỘ tin nhắn loại SUPPORT.
     */
    fun getConversations(myRole: String = "user"): Flow<List<Conversation>> {
        if (currentUid.isEmpty()) return flowOf(emptyList())

        return if (myRole.equals("admin", ignoreCase = true)) {
            val personalFlow = callbackFlow<List<Conversation>> {
                val listener = firestore.collection("conversations")
                    .whereArrayContains("participants", currentUid)
                    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("ChatRepo", "PersonalFlow error: ${error.message}")
                            trySend(emptyList())
                        } else {
                            trySend(snapshot?.toObjects(Conversation::class.java) ?: emptyList())
                        }
                    }
                awaitClose { listener.remove() }
            }.onStart { emit(emptyList()) }

            val supportFlow = callbackFlow<List<Conversation>> {
                val listener = firestore.collection("conversations")
                    .whereEqualTo("type", "SUPPORT")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("ChatRepo", "SupportFlow error: ${error.message}")
                            trySend(emptyList())
                        } else {
                            trySend(snapshot?.toObjects(Conversation::class.java) ?: emptyList())
                        }
                    }
                awaitClose { listener.remove() }
            }.onStart { emit(emptyList()) }

            combine(personalFlow, supportFlow) { personal, support ->
                (personal + support).distinctBy { it.id }
                    .sortedByDescending { it.lastMessageTime.seconds }
            }
        } else {
            callbackFlow<List<Conversation>> {
                val listener = firestore.collection("conversations")
                    .whereArrayContains("participants", currentUid)
                    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) trySend(emptyList())
                        else trySend(snapshot?.toObjects(Conversation::class.java) ?: emptyList())
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    fun getMessages(conversationId: String, limit: Long = 50): Flow<List<ChatMessage>> = callbackFlow {
        if (conversationId.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }
        val listener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) trySend(emptyList())
                else {
                    val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                    trySend(messages.reversed())
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(convId: String, message: ChatMessage) {
        val convRef = firestore.collection("conversations").document(convId)
        val convDoc = convRef.get().await()
        if (!convDoc.exists()) throw Exception("Hội thoại không tồn tại")

        val batch = firestore.batch()
        val msgRef = convRef.collection("messages").document()
        val finalMessage = message.copy(messageId = msgRef.id, senderId = currentUid, delivered = true, createdAt = com.google.firebase.Timestamp.now())
        batch.set(msgRef, finalMessage)

        val updates = mutableMapOf<String, Any>(
            "lastMessage" to when(finalMessage.type) {
                MessageType.IMAGE -> "Đã gửi một ảnh"
                else -> finalMessage.content
            },
            "lastMessageTime" to finalMessage.createdAt,
            "lastSenderId" to currentUid
        )

        val participants = (convDoc.get("participants") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
        val rawRoles = convDoc.get("participantRoles") as? Map<*, *>
        val participantRoles = rawRoles?.map { it.key.toString() to it.value.toString() }?.toMap()?.toMutableMap() ?: mutableMapOf()

        if (!participants.contains(currentUid)) {
            participants.add(currentUid)
            val myUserDoc = firestore.collection("users").document(currentUid).get().await()
            participantRoles[currentUid] = myUserDoc.getString("role") ?: "admin"
            updates["participants"] = participants
            updates["participantRoles"] = participantRoles
        }

        participants.forEach { uid ->
            if (uid != currentUid) updates["unreadCount.$uid"] = FieldValue.increment(1)
        }
        batch.update(convRef, updates)
        batch.commit().await()
    }

    suspend fun markAsSeen(convId: String) {
        if (currentUid.isEmpty() || convId.isBlank()) return
        try {
            firestore.collection("conversations").document(convId).update("unreadCount.$currentUid", 0)
            val snapshot = firestore.collection("conversations").document(convId).collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING).limit(20).get().await()
            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                var hasUpdates = false
                snapshot.documents.forEach { doc ->
                    val senderId = doc.getString("senderId")
                    if (senderId != currentUid && doc.getBoolean("seen") == false) {
                        batch.update(doc.reference, "seen", true)
                        hasUpdates = true
                    }
                }
                if (hasUpdates) batch.commit().await()
            }
        } catch (e: Exception) { Log.e("ChatRepo", "MarkSeen Error: ${e.message}") }
    }

    fun setTypingStatus(convId: String, isTyping: Boolean) {
        if (currentUid.isEmpty() || convId.isBlank()) return
        firestore.collection("conversations").document(convId).update("typing.$currentUid", isTyping)
    }

    suspend fun contactSupport(myRole: String): String {
        val existing = firestore.collection("conversations")
            .whereEqualTo("type", "SUPPORT")
            .whereArrayContains("participants", currentUid)
            .get().await()
        if (!existing.isEmpty) return existing.documents[0].id

        val newDoc = firestore.collection("conversations").document()
        val conversation = Conversation(
            id = newDoc.id,
            participants = listOf(currentUid), 
            participantRoles = mapOf(currentUid to myRole.lowercase()),
            type = "SUPPORT",
            createdAt = com.google.firebase.Timestamp.now(),
            lastMessageTime = com.google.firebase.Timestamp.now(),
            lastMessage = "Yêu cầu hỗ trợ mới"
        )
        newDoc.set(conversation).await()
        return newDoc.id
    }
}
