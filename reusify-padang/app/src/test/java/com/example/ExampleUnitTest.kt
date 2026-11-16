package com.example

import com.example.data.model.ItemInquiry
import com.example.data.model.MarketItem
import com.example.data.model.PremiumRequest
import com.example.data.model.UserProfile
import com.example.data.model.buildChatThreadKey
import com.example.data.model.buildChatThreadsAndNotifications
import com.example.data.model.resolvePremiumMembership
import com.example.data.model.sortMarketplaceItems
import com.example.ui.util.calculateOneMonthExpiry
import com.example.ui.util.decodeImageUrls
import com.example.ui.util.encodeImageUrls
import com.example.ui.util.primaryImageUrl
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun premiumMembership_activeWithinOneMonth_andExpiresAfterOneMonth() {
    val now = Calendar.getInstance().apply { set(2026, Calendar.MAY, 20, 12, 0, 0) }.timeInMillis
    val tenDaysAgo = Timestamp(Date(now - 10L * 86_400_000L))
    val thirtyFiveDaysAgo = Timestamp(Date(now - 35L * 86_400_000L))

    val profile = UserProfile(userId = "u1", displayName = "Budi", isPremium = true)

    val activeInfo =
      resolvePremiumMembership(
        profile = profile,
        requests = listOf(PremiumRequest(id = "r1", userId = "u1", status = "approved", updatedAt = tenDaysAgo)),
        nowMillis = now,
      )
    assertTrue(activeInfo.isActive)
    assertFalse(activeInfo.isExpired)
    assertTrue(activeInfo.daysRemaining in 19..22)
    assertNotNull(activeInfo.expiresAt)

    val expiredInfo =
      resolvePremiumMembership(
        profile = profile,
        requests = listOf(PremiumRequest(id = "r2", userId = "u1", status = "approved", updatedAt = thirtyFiveDaysAgo)),
        nowMillis = now,
      )
    assertFalse(expiredInfo.isActive)
    assertTrue(expiredInfo.isExpired)
    assertEquals(0, expiredInfo.daysRemaining)
  }

  @Test
  fun calculateOneMonthExpiry_addsOneCalendarMonth() {
    val start =
      Calendar.getInstance()
        .apply {
          set(2026, Calendar.MARCH, 15, 10, 0, 0)
          set(Calendar.MILLISECOND, 0)
        }
        .time
    val expected =
      Calendar.getInstance()
        .apply {
          set(2026, Calendar.APRIL, 15, 10, 0, 0)
          set(Calendar.MILLISECOND, 0)
        }
        .time
    val expiry = calculateOneMonthExpiry(Timestamp(start))
    assertEquals(expected.time, expiry?.toDate()?.time)
  }

  @Test
  fun chatThreadsAndNotifications_trackSellerBuyerUnreadAndKeepReadNotifications() {
    val me = "user_me"
    val otherBuyer = "buyer_1"
    val otherSeller = "seller_2"
    val t1 = Timestamp(Date(1_700_000_000_000L))
    val t2 = Timestamp(Date(1_700_000_060_000L))
    val t3 = Timestamp(Date(1_700_000_120_000L))

    val inquiries =
      listOf(
        // Buyer_1 asks me (as Seller) on item_A
        ItemInquiry(
          id = "m1",
          itemId = "item_A",
          itemTitle = "Kipas Kos",
          buyerId = otherBuyer,
          buyerName = "Andi",
          sellerId = me,
          senderId = otherBuyer,
          message = "Masih ada kak?",
          createdAt = t1,
        ),
        // I reply (as Seller) on item_A
        ItemInquiry(
          id = "m2",
          itemId = "item_A",
          itemTitle = "Kipas Kos",
          buyerId = otherBuyer,
          buyerName = "Andi",
          sellerId = me,
          senderId = me,
          message = "Masih ada, bisa COD di UNP.",
          createdAt = t2,
        ),
        // Seller_2 replies to me (as Buyer) on item_B
        ItemInquiry(
          id = "m3",
          itemId = "item_B",
          itemTitle = "Buku Kalkulus",
          buyerId = me,
          buyerName = "Saya",
          sellerId = otherSeller,
          senderId = otherSeller,
          message = "Boleh ketemuan jam 4 sore",
          createdAt = t3,
        ),
      )

    val itemsMap =
      mapOf(
        "item_B" to MarketItem(id = "item_B", sellerId = otherSeller, sellerName = "Rina UNAND", title = "Buku Kalkulus")
      )

    // Initially neither thread is read
    val (threadsUnread, notifsUnread) =
      buildChatThreadsAndNotifications(inquiries, me, emptyMap(), itemsMap)

    assertEquals(2, threadsUnread.size)
    assertEquals(2, notifsUnread.size) // Both incoming messages logged in notifications
    assertEquals(2, threadsUnread.sumOf { it.unreadCount })

    val sellerThread = threadsUnread.first { it.isViewerSeller }
    assertEquals("Andi", sellerThread.buyerName)
    assertEquals(1, sellerThread.unreadCount)

    val buyerThread = threadsUnread.first { !it.isViewerSeller }
    assertEquals("Rina UNAND", buyerThread.sellerName)
    assertEquals(1, buyerThread.unreadCount)

    // After marking seller thread as read, unread drops to 0 for that thread,
    // while incoming notification STILL remains in history with isUnread = false
    val sellerThreadKey = buildChatThreadKey("item_A", otherBuyer, me)
    val (threadsAfterRead, notifsAfterRead) =
      buildChatThreadsAndNotifications(
        inquiries,
        me,
        mapOf(sellerThreadKey to t2.toDate().time),
        itemsMap,
      )

    assertEquals(0, threadsAfterRead.first { it.isViewerSeller }.unreadCount)
    assertEquals(1, threadsAfterRead.first { !it.isViewerSeller }.unreadCount)
    assertEquals(2, notifsAfterRead.size)
    assertFalse(notifsAfterRead.first { it.inquiryId == "m1" }.isUnread)
    assertTrue(notifsAfterRead.first { it.inquiryId == "m3" }.isUnread)
  }

  @Test
  fun multiImageCodec_encodesDecodesAndLimitsToFive() {
    val input =
      listOf(
        "https://example.com/1.jpg",
        "https://example.com/2.jpg",
        "https://example.com/2.jpg", // duplicate removed
        "https://example.com/3.jpg",
        "https://example.com/4.jpg",
        "https://example.com/5.jpg",
        "https://example.com/6.jpg", // exceeds max 5
      )
    val encoded = encodeImageUrls(input)
    val decoded = decodeImageUrls(encoded)
    assertEquals(5, decoded.size)
    assertEquals("https://example.com/1.jpg", primaryImageUrl(encoded))
    assertEquals(listOf("a.jpg", "b.jpg"), decodeImageUrls("a.jpg|b.jpg"))
  }

  @Test
  fun sortMarketplaceItems_putsFeaturedAtVeryTopAcrossAllCampuses() {
    val regularNewest =
      MarketItem(
        id = "item_reg",
        title = "Barang Biasa Terbaru",
        campusCode = "UNP",
        isFeatured = false,
        sellerPremium = false,
        createdAt = Timestamp(Date(3_000_000L)),
      )
    val premiumMemberNonFeatured =
      MarketItem(
        id = "item_prem_no_feat",
        title = "Member Premium Tanpa Sorotan",
        campusCode = "UNAND",
        isFeatured = false,
        sellerPremium = true,
        createdAt = Timestamp(Date(2_000_000L)),
      )
    val seedFeatured =
      MarketItem(
        id = "catalog_1",
        title = "Seed Sorotan",
        campusCode = "PNP",
        isFeatured = true,
        sellerPremium = true,
        createdAt = Timestamp(Date(1_000_000L)),
      )
    val realFeatured =
      MarketItem(
        id = "item_real_feat",
        title = "Iklan Sorotan Mahasiswa",
        campusCode = "UBH",
        isFeatured = true,
        sellerPremium = true,
        createdAt = Timestamp(Date(1_500_000L)),
      )

    val sorted =
      sortMarketplaceItems(
        listOf(regularNewest, seedFeatured, premiumMemberNonFeatured, realFeatured)
      )

    assertEquals(
      listOf("item_real_feat", "catalog_1", "item_prem_no_feat", "item_reg"),
      sorted.map { it.id },
    )
  }
}
