package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Calendar
import java.util.Date

enum class UserRole(val code: String, val label: String, val tagline: String) {
  BUYER(
    code = "buyer",
    label = "Pembeli",
    tagline = "Cari & beli barang bekas kampus dengan QR Code atau COD"
  ),
  SELLER(
    code = "seller",
    label = "Penjual",
    tagline = "Pasang iklan barang, kelola etalase & pantau pesanan"
  );

  companion object {
    fun fromCode(code: String?): UserRole =
      entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BUYER
  }
}

enum class PaymentMethod(val code: String, val label: String, val description: String) {
  QR_CODE(
    code = "QR_CODE",
    label = "QR Code / QRIS Reusify",
    description = "Scan QR Code untuk pembayaran instan & tercatat di riwayat"
  ),
  COD(
    code = "COD",
    label = "COD / Tunai di Kampus",
    description = "Bayar tunai saat perjumpaan/meetup langsung di area kampus"
  )
}

object ItemStatus {
  const val AVAILABLE = "available"
  const val SOLD = "sold"
  const val ARCHIVED = "archived"
}

object TransactionStatus {
  const val PENDING_PAYMENT = "pending_payment"
  const val PAID = "paid"
  const val COMPLETED = "completed"
  const val CANCELLED = "cancelled"
}

@IgnoreExtraProperties
data class Campus(
  @DocumentId val id: String = "",
  val code: String = "",
  val name: String = "",
  val city: String = "Padang",
  val meetupSpot: String = "",
  val active: Boolean = true,
  val createdBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val campusCode: String = "UNP",
  val campusName: String = "Universitas Negeri Padang",
  val faculty: String = "",
  val whatsapp: String = "",
  @get:PropertyName("isPremium")
  @field:PropertyName("isPremium")
  val isPremium: Boolean = false,
  @get:PropertyName("isVerifiedStudent")
  @field:PropertyName("isVerifiedStudent")
  val isVerifiedStudent: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class MarketItem(
  @DocumentId val id: String = "",
  val sellerId: String = "",
  val sellerName: String = "",
  val sellerCampusCode: String = "UNP",
  val sellerPremium: Boolean = false,
  val title: String = "",
  val category: String = "Lainnya",
  val campusCode: String = "UNP",
  val campusName: String = "Universitas Negeri Padang",
  val price: Long = 0L,
  val description: String = "",
  val imageUrl: String = "",
  val status: String = ItemStatus.AVAILABLE,
  val buyerId: String? = null,
  @get:PropertyName("isFeatured")
  @field:PropertyName("isFeatured")
  val isFeatured: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  @get:Exclude
  val isCatalogSeed: Boolean get() = id.startsWith("catalog_")
}

data class SellItemDraft(
  val title: String = "",
  val category: String = "",
  val campusCode: String = "",
  val priceText: String = "",
  val description: String = "",
  val imageUrls: List<String> = emptyList(),
  val useUrlInput: Boolean = false,
  val isFeatured: Boolean = false,
  val hasTouchedFeaturedToggle: Boolean = false,
)

fun sortMarketplaceItems(items: List<MarketItem>): List<MarketItem> =
  items.sortedWith(
    compareByDescending<MarketItem> { it.isFeatured }
      .thenByDescending { !it.isCatalogSeed }
      .thenByDescending { it.sellerPremium }
      .thenByDescending { it.createdAt?.toDate()?.time ?: 0L }
  )

@IgnoreExtraProperties
data class MarketTransaction(
  @DocumentId val id: String = "",
  val transactionCode: String = "",
  val itemId: String = "",
  val itemTitle: String = "",
  val itemImageUrl: String = "",
  val campusCode: String = "UNP",
  val amount: Long = 0L,
  val paymentMethod: String = PaymentMethod.QR_CODE.code,
  val qrPayload: String = "",
  val status: String = TransactionStatus.PENDING_PAYMENT,
  val buyerId: String = "",
  val buyerName: String = "",
  val buyerCampusCode: String = "UNP",
  val sellerId: String = "",
  val sellerName: String = "",
  val meetupNote: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class PremiumRequest(
  @DocumentId val id: String = "",
  val userId: String = "",
  val userName: String = "",
  val userEmail: String = "",
  val campusCode: String = "UNP",
  val planName: String = "Membership Premium 1 Bulan (Rp 15.000)",
  val paymentCode: String = "",
  val status: String = "pending",
  val reviewedBy: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class PremiumMembershipInfo(
  val isActive: Boolean = false,
  val isExpired: Boolean = false,
  val activatedAt: Timestamp? = null,
  val expiresAt: Timestamp? = null,
  val daysRemaining: Int = 0,
  val latestApprovedRequest: PremiumRequest? = null,
)

fun resolvePremiumMembership(
  profile: UserProfile?,
  requests: List<PremiumRequest>,
  nowMillis: Long = System.currentTimeMillis(),
): PremiumMembershipInfo {
  val latestApproved =
    requests
      .filter { it.status == "approved" }
      .maxByOrNull { (it.updatedAt ?: it.createdAt)?.toDate()?.time ?: 0L }

  if (profile?.isPremium != true) {
    return PremiumMembershipInfo(
      isActive = false,
      isExpired = false,
      latestApprovedRequest = latestApproved,
    )
  }

  val startTs =
    latestApproved?.updatedAt
      ?: latestApproved?.createdAt
      ?: profile.updatedAt
      ?: profile.createdAt

  if (startTs == null) {
    val startDate = Date(nowMillis)
    val cal = Calendar.getInstance().apply {
      time = startDate
      add(Calendar.MONTH, 1)
    }
    return PremiumMembershipInfo(
      isActive = true,
      isExpired = false,
      activatedAt = Timestamp(startDate),
      expiresAt = Timestamp(cal.time),
      daysRemaining = 30,
      latestApprovedRequest = latestApproved,
    )
  }

  val cal = Calendar.getInstance().apply {
    time = startTs.toDate()
    add(Calendar.MONTH, 1)
  }
  val expiryDate = cal.time
  val diffMs = expiryDate.time - nowMillis
  return if (diffMs > 0L) {
    val daysLeft = ((diffMs + 86_399_999L) / 86_400_000L).toInt().coerceAtLeast(1)
    PremiumMembershipInfo(
      isActive = true,
      isExpired = false,
      activatedAt = startTs,
      expiresAt = Timestamp(expiryDate),
      daysRemaining = daysLeft,
      latestApprovedRequest = latestApproved,
    )
  } else {
    PremiumMembershipInfo(
      isActive = false,
      isExpired = true,
      activatedAt = startTs,
      expiresAt = Timestamp(expiryDate),
      daysRemaining = 0,
      latestApprovedRequest = latestApproved,
    )
  }
}

@IgnoreExtraProperties
data class ItemInquiry(
  @DocumentId val id: String = "",
  val itemId: String = "",
  val itemTitle: String = "",
  val buyerId: String = "",
  val buyerName: String = "",
  val sellerId: String = "",
  val senderId: String = "",
  val message: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

data class ChatThread(
  val threadKey: String,
  val itemId: String,
  val itemTitle: String,
  val buyerId: String,
  val buyerName: String,
  val sellerId: String,
  val sellerName: String,
  val itemImageUrl: String = "",
  val itemPrice: Long = 0L,
  val campusCode: String = "",
  val messages: List<ItemInquiry>,
  val lastMessage: ItemInquiry,
  val unreadCount: Int = 0,
  val isViewerSeller: Boolean = false,
)

data class ChatNotificationItem(
  val inquiryId: String,
  val threadKey: String,
  val itemId: String,
  val itemTitle: String,
  val buyerId: String,
  val buyerName: String,
  val sellerId: String,
  val senderName: String,
  val counterpartRoleLabel: String,
  val message: String,
  val timestamp: Timestamp?,
  val isUnread: Boolean,
)

fun buildChatThreadKey(itemId: String, buyerId: String, sellerId: String): String =
  "${itemId}__${buyerId}__${sellerId}"

fun buildChatThreadsAndNotifications(
  inquiries: List<ItemInquiry>,
  currentUserId: String,
  readTimestamps: Map<String, Long>,
  itemsById: Map<String, MarketItem> = emptyMap(),
  sellerNamesByTxItem: Map<String, String> = emptyMap(),
): Pair<List<ChatThread>, List<ChatNotificationItem>> {
  if (inquiries.isEmpty() || currentUserId.isBlank()) {
    return emptyList<ChatThread>() to emptyList()
  }

  val grouped =
    inquiries
      .distinctBy { it.id.ifBlank { "${it.itemId}_${it.buyerId}_${it.senderId}_${it.createdAt}_${it.message}" } }
      .groupBy { buildChatThreadKey(it.itemId, it.buyerId, it.sellerId) }

  val threads = mutableListOf<ChatThread>()
  val notifications = mutableListOf<ChatNotificationItem>()

  grouped.forEach { (threadKey, rawMessages) ->
    val sortedMessages =
      rawMessages.sortedBy { it.createdAt?.toDate()?.time ?: 0L }
    val lastMsg = sortedMessages.lastOrNull() ?: return@forEach
    val firstMsg = sortedMessages.first()
    val itemId = firstMsg.itemId
    val buyerId = firstMsg.buyerId
    val sellerId = firstMsg.sellerId
    val item = itemsById[itemId]
    val resolvedTitle = item?.title?.ifBlank { null } ?: lastMsg.itemTitle.ifBlank { "Barang Kampus" }
    val resolvedBuyerName =
      sortedMessages.lastOrNull { it.buyerName.isNotBlank() }?.buyerName ?: "Pembeli"
    val resolvedSellerName =
      item?.sellerName?.ifBlank { null }
        ?: sellerNamesByTxItem[itemId]?.ifBlank { null }
        ?: "Penjual Kampus"
    val isViewerSeller = sellerId == currentUserId
    val lastReadMillis = readTimestamps[threadKey] ?: 0L

    var unreadCount = 0
    sortedMessages.forEach { msg ->
      val msgMillis = msg.createdAt?.toDate()?.time ?: 0L
      val isIncoming = msg.senderId != currentUserId
      if (isIncoming) {
        val isUnread = msgMillis > lastReadMillis
        if (isUnread) unreadCount++
        val senderIsBuyer = msg.senderId == buyerId
        notifications.add(
          ChatNotificationItem(
            inquiryId = msg.id.ifBlank { "${threadKey}_${msgMillis}_${msg.message.hashCode()}" },
            threadKey = threadKey,
            itemId = itemId,
            itemTitle = resolvedTitle,
            buyerId = buyerId,
            buyerName = resolvedBuyerName,
            sellerId = sellerId,
            senderName = if (senderIsBuyer) resolvedBuyerName else resolvedSellerName,
            counterpartRoleLabel = if (senderIsBuyer) "Pembeli" else "Penjual",
            message = msg.message,
            timestamp = msg.createdAt,
            isUnread = isUnread,
          )
        )
      }
    }

    threads.add(
      ChatThread(
        threadKey = threadKey,
        itemId = itemId,
        itemTitle = resolvedTitle,
        buyerId = buyerId,
        buyerName = resolvedBuyerName,
        sellerId = sellerId,
        sellerName = resolvedSellerName,
        itemImageUrl = item?.imageUrl.orEmpty(),
        itemPrice = item?.price ?: 0L,
        campusCode = item?.campusCode.orEmpty(),
        messages = sortedMessages,
        lastMessage = lastMsg,
        unreadCount = unreadCount,
        isViewerSeller = isViewerSeller,
      )
    )
  }

  val sortedThreads =
    threads.sortedByDescending { it.lastMessage.createdAt?.toDate()?.time ?: 0L }
  val sortedNotifications =
    notifications.sortedByDescending { it.timestamp?.toDate()?.time ?: 0L }

  return sortedThreads to sortedNotifications
}
