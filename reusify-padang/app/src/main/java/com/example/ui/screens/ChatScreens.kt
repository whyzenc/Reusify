package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatNotificationItem
import com.example.data.model.ChatThread
import com.example.data.model.MarketItem
import com.example.data.model.UserRole
import com.example.data.model.buildChatThreadKey
import com.example.ui.components.CampusBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MarketItemImage
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.OnOrangeContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.OutlineSoft
import com.example.ui.theme.OutlineWarm
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfaceIvory
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSoftWarm
import com.example.ui.util.formatRupiah
import com.example.ui.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(
  threads: List<ChatThread>,
  notifications: List<ChatNotificationItem>,
  unreadCount: Int,
  currentUserId: String,
  activeRole: UserRole,
  onBack: () -> Unit,
  onThreadClick: (ChatThread) -> Unit,
  onNotificationClick: (ChatNotificationItem) -> Unit,
  onMarkAllRead: () -> Unit,
) {
  val sellerThreads = remember(threads) { threads.filter { it.isViewerSeller } }
  val buyerThreads = remember(threads) { threads.filter { !it.isViewerSeller } }

  var selectedTab by remember(activeRole) {
    mutableIntStateOf(
      when {
        activeRole == UserRole.SELLER && sellerThreads.isNotEmpty() -> 1
        else -> 0
      }
    )
  }

  val tabs = listOf(
    "Semua (${threads.size})",
    "Penjual (${sellerThreads.size})",
    "Pembeli (${buyerThreads.size})",
    if (unreadCount > 0) "Notifikasi ($unreadCount baru)" else "Notifikasi (${notifications.size})",
  )
  val accentColor = if (selectedTab == 1) OrangeDeep else CrimsonPrimary

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "History Chat & Notifikasi",
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
            if (unreadCount > 0) {
              Text(
                text = "$unreadCount pesan belum dibaca",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.88f),
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("chat_history_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = Color.White,
            )
          }
        },
        actions = {
          if (unreadCount > 0) {
            IconButton(
              onClick = onMarkAllRead,
              modifier = Modifier.testTag("chat_mark_all_read_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.DoneAll,
                contentDescription = "Tandai semua terbaca",
                tint = Color.White,
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = accentColor),
      )
    }
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().background(SurfaceIvory).padding(padding)) {
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 12.dp,
        containerColor = SurfacePureWhite,
        contentColor = accentColor,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = accentColor,
          )
        },
      ) {
        tabs.forEachIndexed { idx, label ->
          Tab(
            selected = selectedTab == idx,
            onClick = { selectedTab = idx },
            modifier = Modifier.testTag("chat_tab_$idx"),
            text = {
              Text(
                text = label,
                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
              )
            },
          )
        }
      }

      if (unreadCount > 0 && selectedTab != 3) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = OrangeContainer,
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp)
              .border(1.dp, OrangeDeep.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedTab = 3 },
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = OrangeDeep,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = "$unreadCount notifikasi chat masuk belum dibaca. Ketuk untuk lihat log.",
                style = MaterialTheme.typography.labelMedium,
                color = OnOrangeContainer,
                fontWeight = FontWeight.SemiBold,
              )
            }
            TextButton(onClick = onMarkAllRead) {
              Text("Dibaca", color = CrimsonPrimary, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      if (selectedTab == 3) {
        if (notifications.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyStateView(
              icon = Icons.Filled.NotificationsActive,
              title = "Belum Ada Notifikasi Chat",
              subtitle = "Semua pesan masuk dari pembeli maupun balasan penjual akan tersimpan di sini agar tetap terbaca.",
            )
          }
        } else {
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            item {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceSoftWarm,
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(12.dp)),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "Log Notifikasi Pesan Masuk (${notifications.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                  )
                  if (unreadCount > 0) {
                    Text(
                      text = "Tandai Semua",
                      style = MaterialTheme.typography.labelMedium,
                      color = CrimsonPrimary,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.clickable(onClick = onMarkAllRead),
                    )
                  }
                }
              }
            }
            items(notifications, key = { it.inquiryId }) { notif ->
              ChatNotificationCard(
                notification = notif,
                onClick = { onNotificationClick(notif) },
              )
            }
          }
        }
      } else {
        val visibleThreads =
          when (selectedTab) {
            1 -> sellerThreads
            2 -> buyerThreads
            else -> threads
          }

        if (visibleThreads.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyStateView(
              icon = Icons.Filled.ChatBubbleOutline,
              title =
                when (selectedTab) {
                  1 -> "Belum Ada Chat dari Pembeli"
                  2 -> "Belum Ada Chat ke Penjual"
                  else -> "History Chat Masih Kosong"
                },
              subtitle =
                "Percakapan antara penjual dan pembeli seputar barang & titik COD kampus akan tercatat otomatis di sini.",
            )
          }
        } else {
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            items(visibleThreads, key = { it.threadKey }) { thread ->
              ChatThreadCard(
                thread = thread,
                currentUserId = currentUserId,
                onClick = { onThreadClick(thread) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChatThreadCard(
  thread: ChatThread,
  currentUserId: String,
  onClick: () -> Unit,
) {
  val counterpartName = if (thread.isViewerSeller) thread.buyerName else thread.sellerName
  val roleTag = if (thread.isViewerSeller) "Pembeli" else "Penjual"
  val roleBg = if (thread.isViewerSeller) OrangeContainer else CrimsonContainer
  val roleFg = if (thread.isViewerSeller) OrangeDeep else CrimsonPrimary
  val lastPrefix = if (thread.lastMessage.senderId == currentUserId) "Anda: " else ""

  Card(
    shape = RoundedCornerShape(16.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = if (thread.unreadCount > 0) SurfaceBlush else SurfacePureWhite
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (thread.unreadCount > 0) 2.dp else 1.dp),
    modifier =
      Modifier
        .fillMaxWidth()
        .border(
          width = if (thread.unreadCount > 0) 1.5.dp else 1.dp,
          color = if (thread.unreadCount > 0) OrangeDeep.copy(alpha = 0.6f) else OutlineWarm,
          shape = RoundedCornerShape(16.dp),
        )
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag("chat_thread_${thread.threadKey}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      MarketItemImage(
        imageUrl = thread.itemImageUrl,
        contentDescription = thread.itemTitle,
        modifier = Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)),
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
          ) {
            Text(
              text = counterpartName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Surface(shape = RoundedCornerShape(6.dp), color = roleBg) {
              Text(
                text = roleTag,
                style = MaterialTheme.typography.labelSmall,
                color = roleFg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
          if (thread.unreadCount > 0) {
            Surface(shape = CircleShape, color = CrimsonPrimary) {
              Text(
                text = "${thread.unreadCount} baru",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (thread.campusCode.isNotBlank()) {
            CampusBadge(campusCode = thread.campusCode)
          }
          Text(
            text = thread.itemTitle,
            style = MaterialTheme.typography.labelMedium,
            color = CrimsonPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Text(
          text = "$lastPrefix${thread.lastMessage.message}",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = if (thread.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        Text(
          text = "${formatTimestamp(thread.lastMessage.createdAt)} • ${thread.messages.size} pesan",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ChatNotificationCard(
  notification: ChatNotificationItem,
  onClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = if (notification.isUnread) OrangeContainer.copy(alpha = 0.45f) else SurfacePureWhite
      ),
    modifier =
      Modifier
        .fillMaxWidth()
        .border(
          width = if (notification.isUnread) 1.5.dp else 1.dp,
          color = if (notification.isUnread) OrangeDeep else OutlineWarm,
          shape = RoundedCornerShape(14.dp),
        )
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = Icons.Filled.NotificationsActive,
            contentDescription = null,
            tint = if (notification.isUnread) OrangeDeep else CrimsonPrimary,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "${notification.senderName} (${notification.counterpartRoleLabel})",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (notification.isUnread) CrimsonPrimary else SuccessGreenContainer,
        ) {
          Text(
            text = if (notification.isUnread) "BELUM DIBACA" else "Terbaca",
            style = MaterialTheme.typography.labelSmall,
            color = if (notification.isUnread) Color.White else SuccessGreen,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
      }
      Text(
        text = "Barang: ${notification.itemTitle}",
        style = MaterialTheme.typography.labelSmall,
        color = CrimsonPrimary,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "\"${notification.message}\"",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = formatTimestamp(notification.timestamp),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "Buka & Balas ->",
          style = MaterialTheme.typography.labelSmall,
          color = OrangeDeep,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatDetailScreen(
  itemId: String,
  buyerId: String,
  sellerId: String,
  currentUserId: String,
  thread: ChatThread?,
  fallbackItem: MarketItem?,
  onBack: () -> Unit,
  onOpenItem: (String) -> Unit,
  onMarkRead: (String) -> Unit,
  onSetActiveThread: (String?) -> Unit,
  onSendMessage: (itemId: String, itemTitle: String, buyerId: String, buyerName: String, sellerId: String, message: String) -> Unit,
) {
  val threadKey = remember(itemId, buyerId, sellerId) { buildChatThreadKey(itemId, buyerId, sellerId) }
  val isViewerSeller = sellerId == currentUserId
  val itemTitle = thread?.itemTitle ?: fallbackItem?.title ?: "Barang Kampus"
  val itemPrice = thread?.itemPrice?.takeIf { it > 0L } ?: fallbackItem?.price ?: 0L
  val itemImageUrl = thread?.itemImageUrl?.ifBlank { null } ?: fallbackItem?.imageUrl.orEmpty()
  val campusCode = thread?.campusCode?.ifBlank { null } ?: fallbackItem?.campusCode.orEmpty()
  val buyerName = thread?.buyerName?.ifBlank { null } ?: "Pembeli"
  val sellerName = thread?.sellerName?.ifBlank { null } ?: fallbackItem?.sellerName ?: "Penjual Kampus"
  val counterpartName = if (isViewerSeller) buyerName else sellerName
  val messages = thread?.messages.orEmpty()

  var messageInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  DisposableEffect(threadKey) {
    onSetActiveThread(threadKey)
    onDispose {
      onSetActiveThread(null)
    }
  }

  LaunchedEffect(threadKey, messages.size) {
    onMarkRead(threadKey)
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
    }
  }

  val topBarColor = if (isViewerSeller) OrangeDeep else CrimsonPrimary

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = counterpartName,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "${if (isViewerSeller) "Pembeli" else "Penjual"} • $itemTitle",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.86f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("chat_detail_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = Color.White,
            )
          }
        },
        actions = {
          IconButton(onClick = { onOpenItem(itemId) }) {
            Icon(
              imageVector = Icons.Filled.Storefront,
              contentDescription = "Lihat detail barang",
              tint = Color.White,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor),
      )
    },
    bottomBar = {
      Surface(
        shadowElevation = 8.dp,
        color = SurfacePureWhite,
      ) {
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
              .imePadding()
              .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          val quickReplies =
            if (isViewerSeller) {
              listOf(
                "Halo kak, barang masih tersedia ya",
                "Bisa COD di area kampus hari ini",
                "Silakan checkout lewat QR Code / COD",
              )
            } else {
              listOf(
                "Halo kak, barang ini masih ada?",
                "Bisa COD di kampus?",
                "Saya mau bayar pakai QR Code ya",
              )
            }
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            quickReplies.forEach { quick ->
              Surface(
                shape = CircleShape,
                color = if (isViewerSeller) OrangeContainer else CrimsonContainer.copy(alpha = 0.7f),
                modifier =
                  Modifier
                    .clip(CircleShape)
                    .clickable {
                      onSendMessage(itemId, itemTitle, buyerId, buyerName, sellerId, quick)
                    },
              ) {
                Text(
                  text = quick,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isViewerSeller) OrangeDeep else CrimsonPrimary,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = messageInput,
              onValueChange = { messageInput = it },
              placeholder = {
                Text(
                  if (isViewerSeller) "Balas chat pembeli..." else "Tulis pesan ke penjual..."
                )
              },
              modifier = Modifier.weight(1f).testTag("chat_detail_input"),
              shape = RoundedCornerShape(14.dp),
              maxLines = 3,
            )
            IconButton(
              onClick = {
                if (messageInput.isNotBlank()) {
                  onSendMessage(itemId, itemTitle, buyerId, buyerName, sellerId, messageInput)
                  messageInput = ""
                }
              },
              modifier = Modifier.size(48.dp).testTag("chat_detail_send_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Kirim pesan",
                tint = topBarColor,
              )
            }
          }
        }
      }
    },
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(SurfaceIvory)
          .padding(padding),
    ) {
      // Compact Item Summary Strip
      Surface(
        color = SurfacePureWhite,
        modifier =
          Modifier
            .fillMaxWidth()
            .border(0.5.dp, OutlineSoft)
            .clickable { onOpenItem(itemId) },
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MarketItemImage(
            imageUrl = itemImageUrl,
            contentDescription = itemTitle,
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)),
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = itemTitle,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              if (itemPrice > 0L) {
                Text(
                  text = formatRupiah(itemPrice),
                  style = MaterialTheme.typography.labelMedium,
                  color = CrimsonPrimary,
                  fontWeight = FontWeight.Bold,
                )
              }
              if (campusCode.isNotBlank()) {
                CampusBadge(campusCode = campusCode)
              }
            }
          }
          Text(
            text = "Lihat",
            style = MaterialTheme.typography.labelMedium,
            color = OrangeDeep,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          EmptyStateView(
            icon = Icons.Filled.ChatBubbleOutline,
            title = "Mulai Percakapan",
            subtitle = "Semua pesan antara penjual & pembeli tersimpan di History Chat.",
          )
        }
      } else {
        LazyColumn(
          state = listState,
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize(),
        ) {
          items(messages, key = { it.id.ifBlank { "${it.senderId}_${it.createdAt}_${it.message}" } }) { msg ->
            val isMine = msg.senderId == currentUserId
            val senderLabel =
              if (isMine) "Anda"
              else if (msg.senderId == buyerId) buyerName
              else sellerName

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            ) {
              Surface(
                shape = RoundedCornerShape(
                  topStart = 14.dp,
                  topEnd = 14.dp,
                  bottomStart = if (isMine) 14.dp else 4.dp,
                  bottomEnd = if (isMine) 4.dp else 14.dp,
                ),
                color = if (isMine) topBarColor else SurfacePureWhite,
                shadowElevation = 1.dp,
                modifier =
                  Modifier
                    .widthIn(max = 290.dp)
                    .then(
                      if (!isMine) Modifier.border(1.dp, OutlineWarm, RoundedCornerShape(14.dp, 14.dp, 14.dp, 4.dp))
                      else Modifier
                    ),
              ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                  Text(
                    text = senderLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isMine) Color.White.copy(alpha = 0.85f) else CrimsonPrimary,
                  )
                  Spacer(Modifier.height(2.dp))
                  Text(
                    text = msg.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                  )
                  Spacer(Modifier.height(3.dp))
                  Text(
                    text = formatTimestamp(msg.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color =
                      if (isMine) Color.White.copy(alpha = 0.72f)
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
