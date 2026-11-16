package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.CatalogSeedData
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.Campus
import com.example.data.model.ItemInquiry
import com.example.data.model.ItemStatus
import com.example.data.model.MarketItem
import com.example.data.model.MarketTransaction
import com.example.data.model.PaymentMethod
import com.example.data.model.PremiumRequest
import com.example.data.model.TransactionStatus
import com.example.data.model.UserProfile
import com.example.data.model.sortMarketplaceItems
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ReusifyRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth,
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("Pengguna wajib login Google terlebih dahulu.")

  suspend fun checkIsAdmin(uid: String, email: String?, emailVerified: Boolean): Boolean {
    if (emailVerified && email != null && email.equals(CatalogSeedData.ADMIN_EMAIL, ignoreCase = true)) {
      return true
    }
    return try {
      val snap = db.collection("admins").document(uid).get().await()
      snap.exists()
    } catch (_: Exception) {
      false
    }
  }

  // --- USER PROFILE ---

  fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
    val path = "users/$uid"
    emitAll(
      db.collection("users")
        .document(uid)
        .snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          } else {
            null
          }
        }
        .catch { error ->
          handleFlowError(error, OperationType.GET, path, uid)
          throw error
        }
    )
  }

  suspend fun ensureUserProfile(
    uid: String,
    displayName: String,
    email: String,
    customPhone: String = "",
  ): Result<UserProfile> = runCatching {
    val docRef = db.collection("users").document(uid)
    try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        existing.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          ?: UserProfile(userId = uid, displayName = displayName, email = email)
      } else {
        val safeName = displayName.ifBlank { email.substringBefore("@").ifBlank { "Mahasiswa Padang" } }.take(80)
        val safePhone = customPhone.trim().take(25)
        val payload =
          buildMap<String, Any> {
            put("userId", uid)
            put("displayName", safeName)
            put("email", email.take(120))
            put("campusCode", "UNP")
            put("campusName", "Universitas Negeri Padang")
            if (safePhone.isNotEmpty()) {
              put("whatsapp", safePhone)
            }
            put("isPremium", false)
            put("isVerifiedStudent", true)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
          }
        docRef.set(payload).await()
        UserProfile(
          id = uid,
          userId = uid,
          displayName = safeName,
          email = email.take(120),
          campusCode = "UNP",
          campusName = "Universitas Negeri Padang",
          whatsapp = safePhone,
          isPremium = false,
          isVerifiedStudent = true,
        )
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      throw e
    }
  }

  suspend fun updateUserProfile(
    uid: String,
    displayName: String,
    campusCode: String,
    campusName: String,
    faculty: String,
    whatsapp: String,
  ): Result<Unit> = runCatching {
    requireUserId()
    val docRef = db.collection("users").document(uid)
    val payload =
      mapOf(
        "displayName" to displayName.trim().take(80),
        "campusCode" to campusCode.trim().take(15),
        "campusName" to campusName.trim().take(100),
        "faculty" to faculty.trim().take(80),
        "whatsapp" to whatsapp.trim().take(25),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    try {
      docRef.update(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      throw e
    }
  }

  fun observeAllUsersAdmin(): Flow<List<UserProfile>> = flow {
    val uid = auth.currentUser?.uid
    val path = "users"
    emitAll(
      db.collection("users")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.displayName.lowercase() }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  suspend fun setUserPremiumAdmin(userId: String, isPremium: Boolean): Result<Unit> = runCatching {
    requireUserId()
    val docRef = db.collection("users").document(userId)
    try {
      docRef.update(
        mapOf(
          "isPremium" to isPremium,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun setUserVerifiedAdmin(userId: String, isVerified: Boolean): Result<Unit> = runCatching {
    requireUserId()
    val docRef = db.collection("users").document(userId)
    try {
      docRef.update(
        mapOf(
          "isVerifiedStudent" to isVerified,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      throw e
    }
  }

  // --- CAMPUSES (UNP, PNP, UNAND, UBH + ADMIN MANAGED) ---

  fun observeActiveCampuses(): Flow<List<Campus>> = flow {
    val uid = auth.currentUser?.uid
    val path = "campuses"
    emitAll(
      db.collection("campuses")
        .whereEqualTo("active", true)
        .snapshots()
        .map { snapshot ->
          val remote =
            snapshot.toObjects(Campus::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          mergeCampusesWithDefaults(remote)
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeAllCampusesAdmin(): Flow<List<Campus>> = flow {
    val uid = auth.currentUser?.uid
    val path = "campuses"
    emitAll(
      db.collection("campuses")
        .snapshots()
        .map { snapshot ->
          val remote =
            snapshot.toObjects(Campus::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          if (remote.isEmpty()) CatalogSeedData.defaultCampuses else remote.sortedBy { it.code }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  private fun mergeCampusesWithDefaults(remote: List<Campus>): List<Campus> {
    val remoteCodes = remote.map { it.code.uppercase() }.toSet()
    val missingDefaults = CatalogSeedData.defaultCampuses.filter { it.code.uppercase() !in remoteCodes }
    return (missingDefaults + remote).sortedWith(
      compareBy<Campus> {
        when (it.code.uppercase()) {
          "UNP" -> 0
          "PNP" -> 1
          "UNAND" -> 2
          "UBH" -> 3
          else -> 4
        }
      }.thenBy { it.code }
    )
  }

  suspend fun seedDefaultCampusesIfAdmin(uid: String): Result<Unit> = runCatching {
    val existing = try {
      db.collection("campuses").get().await().documents.map { it.id }.toSet()
    } catch (_: Exception) {
      emptySet()
    }
    for (campus in CatalogSeedData.defaultCampuses) {
      if (campus.id !in existing) {
        val docRef = db.collection("campuses").document(campus.id)
        try {
          docRef.set(
            mapOf(
              "code" to campus.code,
              "name" to campus.name,
              "city" to campus.city,
              "meetupSpot" to campus.meetupSpot,
              "active" to true,
              "createdBy" to uid,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        } catch (e: Exception) {
          handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
        }
      }
    }
  }

  suspend fun addOrUpdateCampus(
    campusId: String?,
    code: String,
    name: String,
    meetupSpot: String,
    active: Boolean,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val cleanCode = code.trim().uppercase().replace(Regex("[^A-Z0-9_\\-]"), "").take(15)
    require(cleanCode.length >= 2) { "Kode kampus minimal 2 huruf/angka" }
    val docId = campusId?.takeIf { it.isNotBlank() } ?: cleanCode
    val docRef = db.collection("campuses").document(docId)
    try {
      val snap = docRef.get().await()
      if (snap.exists()) {
        docRef.update(
          mapOf(
            "code" to cleanCode,
            "name" to name.trim().take(100),
            "city" to "Padang",
            "meetupSpot" to meetupSpot.trim().take(120),
            "active" to active,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      } else {
        docRef.set(
          mapOf(
            "code" to cleanCode,
            "name" to name.trim().take(100),
            "city" to "Padang",
            "meetupSpot" to meetupSpot.trim().take(120),
            "active" to active,
            "createdBy" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      docId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      throw e
    }
  }

  suspend fun deleteCampus(campusId: String): Result<Unit> = runCatching {
    requireUserId()
    val docRef = db.collection("campuses").document(campusId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
      throw e
    }
  }

  // --- MARKETPLACE ITEMS ---

  fun observeAvailableItems(): Flow<List<MarketItem>> = flow {
    val uid = auth.currentUser?.uid
    val path = "items"
    emitAll(
      db.collection("items")
        .whereEqualTo("status", ItemStatus.AVAILABLE)
        .snapshots()
        .map { snapshot ->
          val remoteItems =
            snapshot.toObjects(MarketItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          sortMarketplaceItems(remoteItems + CatalogSeedData.catalogItems)
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeMySellingItems(uid: String): Flow<List<MarketItem>> = flow {
    val path = "items"
    emitAll(
      db.collection("items")
        .whereEqualTo("sellerId", uid)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(MarketItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeAllItemsAdmin(): Flow<List<MarketItem>> = flow {
    val uid = auth.currentUser?.uid
    val path = "items"
    emitAll(
      db.collection("items")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(MarketItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  suspend fun getItemById(itemId: String): Result<MarketItem?> = runCatching {
    if (itemId.startsWith("catalog_")) {
      return@runCatching CatalogSeedData.catalogItems.firstOrNull { it.id == itemId }
    }
    requireUserId()
    val docRef = db.collection("items").document(itemId)
    try {
      val snap = docRef.get().await()
      if (snap.exists()) {
        snap.toObject(MarketItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      } else {
        null
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path, auth)
      throw e
    }
  }

  suspend fun createItem(
    title: String,
    category: String,
    campusCode: String,
    campusName: String,
    price: Long,
    description: String,
    imageUrl: String,
    sellerName: String,
    sellerCampusCode: String,
    sellerPremium: Boolean,
    isFeatured: Boolean,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("items").document()
    val payload =
      mapOf(
        "sellerId" to uid,
        "sellerName" to sellerName.trim().ifBlank { "Mahasiswa Padang" }.take(80),
        "sellerCampusCode" to sellerCampusCode.trim().ifBlank { "UNP" }.take(15),
        "sellerPremium" to sellerPremium,
        "title" to title.trim().take(120),
        "category" to category.trim().take(40),
        "campusCode" to campusCode.trim().take(15),
        "campusName" to campusName.trim().take(100),
        "price" to price,
        "description" to description.trim().take(1000),
        "imageUrl" to imageUrl.take(150000),
        "status" to ItemStatus.AVAILABLE,
        "isFeatured" to (sellerPremium && isFeatured),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun updateMyItem(item: MarketItem): Result<Unit> = runCatching {
    requireUserId()
    require(item.id.isNotBlank()) { "Item ID tidak valid" }
    val docRef = db.collection("items").document(item.id)
    val payload =
      mapOf(
        "title" to item.title.trim().take(120),
        "category" to item.category.trim().take(40),
        "campusCode" to item.campusCode.trim().take(15),
        "campusName" to item.campusName.trim().take(100),
        "price" to item.price,
        "description" to item.description.trim().take(1000),
        "imageUrl" to item.imageUrl.take(150000),
        "status" to item.status,
        "isFeatured" to item.isFeatured,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    try {
      docRef.update(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun updateItemModerationAdmin(
    itemId: String,
    isFeatured: Boolean,
    status: String,
  ): Result<Unit> = runCatching {
    requireUserId()
    val docRef = db.collection("items").document(itemId)
    try {
      docRef.update(
        mapOf(
          "isFeatured" to isFeatured,
          "status" to status,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
    requireUserId()
    val docRef = db.collection("items").document(itemId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
      throw e
    }
  }

  // --- TRANSACTIONS (BUYER & SELLER HISTORY + QR CODE PAYMENT) ---

  fun observeBuyerTransactions(uid: String): Flow<List<MarketTransaction>> = flow {
    val path = "transactions"
    emitAll(
      db.collection("transactions")
        .whereEqualTo("buyerId", uid)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(MarketTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeSellerTransactions(uid: String): Flow<List<MarketTransaction>> = flow {
    val path = "transactions"
    emitAll(
      db.collection("transactions")
        .whereEqualTo("sellerId", uid)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(MarketTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeAllTransactionsAdmin(): Flow<List<MarketTransaction>> = flow {
    val uid = auth.currentUser?.uid
    val path = "transactions"
    emitAll(
      db.collection("transactions")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(MarketTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  suspend fun getTransactionById(txId: String): Result<MarketTransaction?> = runCatching {
    requireUserId()
    val docRef = db.collection("transactions").document(txId)
    try {
      val snap = docRef.get().await()
      if (snap.exists()) {
        snap.toObject(MarketTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      } else {
        null
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path, auth)
      throw e
    }
  }

  suspend fun purchaseItem(
    item: MarketItem,
    buyerProfile: UserProfile,
    paymentMethod: PaymentMethod,
    meetupNote: String,
  ): Result<MarketTransaction> = runCatching {
    val uid = requireUserId()
    require(item.sellerId != uid) { "Anda tidak dapat membeli barang jualan sendiri." }

    val txRef = db.collection("transactions").document()
    val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    val shortSuffix = UUID.randomUUID().toString().take(4).uppercase()
    val txCode = "RSF-$dateStamp-$shortSuffix"
    val qrPayload =
      "REUSIFY-PADANG|CODE:$txCode|TX:${txRef.id}|ITEM:${item.title.take(40)}|AMT:${item.price}|CAMPUS:${item.campusCode}|METHOD:${paymentMethod.code}"
    val initialStatus = TransactionStatus.PENDING_PAYMENT

    val txPayload =
      mapOf(
        "transactionCode" to txCode,
        "itemId" to item.id,
        "itemTitle" to item.title.take(120),
        "itemImageUrl" to item.imageUrl.take(150000),
        "campusCode" to item.campusCode.take(15),
        "amount" to item.price,
        "paymentMethod" to paymentMethod.code,
        "qrPayload" to qrPayload.take(500),
        "status" to initialStatus,
        "buyerId" to uid,
        "buyerName" to buyerProfile.displayName.ifBlank { "Mahasiswa" }.take(80),
        "buyerCampusCode" to buyerProfile.campusCode.ifBlank { "UNP" }.take(15),
        "sellerId" to item.sellerId,
        "sellerName" to item.sellerName.ifBlank { "Penjual Kampus" }.take(80),
        "meetupNote" to meetupNote.trim().take(300),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )

    try {
      if (item.isCatalogSeed) {
        txRef.set(txPayload).await()
      } else {
        val batch = db.batch()
        val itemRef = db.collection("items").document(item.id)
        batch.update(
          itemRef,
          mapOf(
            "status" to ItemStatus.SOLD,
            "buyerId" to uid,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
        batch.set(txRef, txPayload)
        batch.commit().await()
      }

      MarketTransaction(
        id = txRef.id,
        transactionCode = txCode,
        itemId = item.id,
        itemTitle = item.title,
        itemImageUrl = item.imageUrl,
        campusCode = item.campusCode,
        amount = item.price,
        paymentMethod = paymentMethod.code,
        qrPayload = qrPayload,
        status = initialStatus,
        buyerId = uid,
        buyerName = buyerProfile.displayName.ifBlank { "Mahasiswa" },
        buyerCampusCode = buyerProfile.campusCode.ifBlank { "UNP" },
        sellerId = item.sellerId,
        sellerName = item.sellerName,
        meetupNote = meetupNote.trim(),
        createdAt = Timestamp.now(),
        updatedAt = Timestamp.now(),
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, txRef.path, auth)
      throw e
    }
  }

  suspend fun updateTransactionStatus(
    txId: String,
    newStatus: String,
    meetupNote: String? = null,
  ): Result<Unit> = runCatching {
    requireUserId()
    val docRef = db.collection("transactions").document(txId)
    val updates = mutableMapOf<String, Any>(
      "status" to newStatus,
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    if (meetupNote != null) {
      updates["meetupNote"] = meetupNote.trim().take(300)
    }
    try {
      docRef.update(updates).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      throw e
    }
  }

  // --- PREMIUM REQUESTS (ADMIN MANAGED) ---

  fun observeMyPremiumRequests(uid: String): Flow<List<PremiumRequest>> = flow {
    val path = "premium_requests"
    emitAll(
      db.collection("premium_requests")
        .whereEqualTo("userId", uid)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(PremiumRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeAllPremiumRequestsAdmin(): Flow<List<PremiumRequest>> = flow {
    val uid = auth.currentUser?.uid
    val path = "premium_requests"
    emitAll(
      db.collection("premium_requests")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(PremiumRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  suspend fun submitPremiumRequest(
    profile: UserProfile,
    planName: String,
    paymentCode: String,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("premium_requests").document()
    val payload =
      mapOf(
        "userId" to uid,
        "userName" to profile.displayName.ifBlank { "Mahasiswa" }.take(80),
        "userEmail" to profile.email.take(120),
        "campusCode" to profile.campusCode.ifBlank { "UNP" }.take(15),
        "planName" to planName.take(60),
        "paymentCode" to paymentCode.trim().take(60),
        "status" to "pending",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun reviewPremiumRequest(request: PremiumRequest, approve: Boolean): Result<Unit> = runCatching {
    val adminUid = requireUserId()
    val reqRef = db.collection("premium_requests").document(request.id)
    val userRef = db.collection("users").document(request.userId)
    try {
      val batch = db.batch()
      batch.update(
        reqRef,
        mapOf(
          "status" to if (approve) "approved" else "rejected",
          "reviewedBy" to adminUid,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      if (approve) {
        batch.update(
          userRef,
          mapOf(
            "isPremium" to true,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.commit().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, reqRef.path, auth)
      throw e
    }
  }

  // --- INQUIRIES / CHAT ---

  fun observeBuyerInquiries(uid: String): Flow<List<ItemInquiry>> = flow {
    val path = "inquiries"
    emitAll(
      db.collection("inquiries")
        .whereEqualTo("buyerId", uid)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(ItemInquiry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeSellerInquiries(uid: String): Flow<List<ItemInquiry>> = flow {
    val path = "inquiries"
    emitAll(
      db.collection("inquiries")
        .whereEqualTo("sellerId", uid)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(ItemInquiry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, uid)
          throw error
        }
    )
  }

  fun observeInquiriesForItem(
    itemId: String,
    userId: String,
    isSeller: Boolean,
  ): Flow<List<ItemInquiry>> = flow {
    val path = "inquiries"
    val baseQuery =
      if (isSeller) {
        db.collection("inquiries")
          .whereEqualTo("sellerId", userId)
          .whereEqualTo("itemId", itemId)
      } else {
        db.collection("inquiries")
          .whereEqualTo("buyerId", userId)
          .whereEqualTo("itemId", itemId)
      }
    emitAll(
      baseQuery
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(ItemInquiry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, path, userId)
          throw error
        }
    )
  }

  suspend fun sendChatMessage(
    itemId: String,
    itemTitle: String,
    buyerId: String,
    buyerName: String,
    sellerId: String,
    message: String,
  ): Result<String> = runCatching {
    val senderUid = requireUserId()
    val docRef = db.collection("inquiries").document()
    val payload =
      mapOf(
        "itemId" to itemId.take(128),
        "itemTitle" to itemTitle.ifBlank { "Barang Kampus" }.take(120),
        "buyerId" to buyerId.take(128),
        "buyerName" to buyerName.ifBlank { "Pembeli" }.take(80),
        "sellerId" to sellerId.take(128),
        "senderId" to senderUid,
        "message" to message.trim().take(500),
        "createdAt" to FieldValue.serverTimestamp(),
      )
    try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      throw e
    }
  }

  suspend fun sendInquiry(
    item: MarketItem,
    buyerId: String,
    buyerName: String,
    message: String,
  ): Result<String> =
    sendChatMessage(
      itemId = item.id,
      itemTitle = item.title,
      buyerId = buyerId,
      buyerName = buyerName,
      sellerId = item.sellerId,
      message = message,
    )

  private fun handleFlowError(error: Throwable, op: OperationType, path: String, expectedUid: String?) {
    val firestoreError =
      generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
    if (firestoreError == null && error is CancellationException) return
    val ex = firestoreError ?: (error as? Exception)
    if (ex != null && expectedUid != null && auth.currentUser?.uid == expectedUid) {
      handleFirestoreError(ex, op, path, auth)
    }
  }
}
