package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemInquiry
import com.example.data.model.MarketItem
import com.example.data.model.PaymentMethod
import com.example.ui.components.CampusBadge
import com.example.ui.components.PremiumBadge
import com.example.ui.components.SwipeableItemImageCarousel
import com.example.ui.theme.MaroonContainer
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OutlineSoft
import com.example.ui.theme.SurfaceBlush
import com.example.ui.util.formatRupiah
import com.example.ui.util.formatTimestamp
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemDetailScreen(
  item: MarketItem?,
  currentUserId: String,
  activeRole: com.example.data.model.UserRole = com.example.data.model.UserRole.BUYER,
  isBusy: Boolean,
  inquiriesFlow: Flow<List<ItemInquiry>>,
  itemChatCount: Int = 0,
  onBack: () -> Unit,
  onBuyClick: (PaymentMethod, String) -> Unit,
  onSendInquiry: (String) -> Unit,
  onManageMyItem: () -> Unit,
  onOpenSellerChats: () -> Unit = {},
  onMarkItemThreadRead: () -> Unit = {},
) {
  var selectedMethod by remember { mutableStateOf(PaymentMethod.QR_CODE) }
  var meetupNote by remember { mutableStateOf("") }
  var showChatSheet by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Detail Barang",
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = Color.White,
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor =
              if (activeRole == com.example.data.model.UserRole.SELLER)
                com.example.ui.theme.OrangeDeep
              else MaroonPrimary
          ),
      )
    },
    bottomBar = {
      if (item != null) {
        val isOwnItem = item.sellerId == currentUserId
        Surface(
          shadowElevation = 10.dp,
          color = MaterialTheme.colorScheme.surface,
        ) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (isOwnItem) {
              OutlinedButton(
                onClick = onOpenSellerChats,
                modifier = Modifier.height(50.dp).testTag("seller_item_chats_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaroonPrimary),
              ) {
                Icon(
                  imageVector = Icons.Filled.ChatBubbleOutline,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (itemChatCount > 0) "Chat ($itemChatCount)" else "Chat Pembeli",
                  fontWeight = FontWeight.Bold,
                )
              }
              Button(
                onClick = onManageMyItem,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
              ) {
                Text("Kelola Barang", fontWeight = FontWeight.Bold)
              }
            } else {
              OutlinedButton(
                onClick = { showChatSheet = true },
                modifier = Modifier.height(50.dp).testTag("chat_seller_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaroonPrimary),
              ) {
                Icon(
                  imageVector = Icons.Filled.ChatBubbleOutline,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tanya", fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = { onBuyClick(selectedMethod, meetupNote) },
                enabled = !isBusy,
                modifier = Modifier.weight(1f).height(50.dp).testTag("buy_item_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
              ) {
                if (isBusy) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                  )
                } else {
                  Icon(
                    imageVector =
                      if (selectedMethod == PaymentMethod.QR_CODE) Icons.Filled.QrCode2
                      else Icons.Filled.Handshake,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text =
                      if (selectedMethod == PaymentMethod.QR_CODE) "Bayar QR Code"
                      else "Pesan COD",
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }
        }
      }
    },
  ) { padding ->
    if (item == null) {
      Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(color = MaroonPrimary)
      }
    } else {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
      ) {
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
          SwipeableItemImageCarousel(
            imageUrl = item.imageUrl,
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
          )
        }

        Column(
          modifier = Modifier.widthIn(max = 640.dp).padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CampusBadge(campusCode = item.campusCode, showIcon = true)
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
              ) {
                Text(
                  text = item.category,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
              if (item.sellerPremium) {
                PremiumBadge()
              }
            }
            Text(
              text = formatRupiah(item.price),
              style = MaterialTheme.typography.headlineMedium,
              color = MaroonPrimary,
              fontWeight = FontWeight.Bold,
            )
          }

          Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )

          HorizontalDivider(color = OutlineSoft)

          Text(
            text = "Deskripsi Barang",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = item.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          // Seller Card matching Screenshot 4
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceBlush,
            modifier = Modifier.fillMaxWidth().border(1.dp, OutlineSoft, RoundedCornerShape(14.dp)),
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = MaroonPrimary,
                modifier = Modifier.size(44.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                  )
                }
              }
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = "Penjual: ${item.sellerName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                  Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Terverifikasi",
                    tint = MaroonPrimary,
                    modifier = Modifier.size(16.dp),
                  )
                }
                Text(
                  text = "Terverifikasi Mahasiswa • ${item.campusName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          if (item.sellerId != currentUserId) {
            if (activeRole == com.example.data.model.UserRole.SELLER) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = com.example.ui.theme.OrangeContainer,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text = "Info Mode Penjual: Anda melihat produk mahasiswa lain sebagai referensi pasar (tetap bisa dibeli langsung).",
                  style = MaterialTheme.typography.bodySmall,
                  color = com.example.ui.theme.OnOrangeContainer,
                  modifier = Modifier.padding(12.dp),
                )
              }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Metode Pembayaran",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Pilih cara transaksi yang disepakati dengan penjual:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PaymentMethodOptionCard(
              method = PaymentMethod.QR_CODE,
              icon = Icons.Filled.QrCode2,
              selected = selectedMethod == PaymentMethod.QR_CODE,
              onSelect = { selectedMethod = PaymentMethod.QR_CODE },
            )

            PaymentMethodOptionCard(
              method = PaymentMethod.COD,
              icon = Icons.Filled.Handshake,
              selected = selectedMethod == PaymentMethod.COD,
              onSelect = { selectedMethod = PaymentMethod.COD },
            )

            OutlinedTextField(
              value = meetupNote,
              onValueChange = { meetupNote = it },
              label = { Text("Catatan Titik Temu / Pesan (Opsional)") },
              placeholder = { Text("Contoh: Ketemuan di Gerbang ${item.campusCode} jam 16.00") },
              modifier = Modifier.fillMaxWidth().testTag("detail_meetup_note"),
              shape = RoundedCornerShape(12.dp),
              maxLines = 2,
            )
          }
        }
      }
    }
  }

  if (showChatSheet && item != null) {
    val inquiries by inquiriesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var msgText by remember { mutableStateOf("") }

    LaunchedEffect(showChatSheet, inquiries.size) {
      onMarkItemThreadRead()
    }

    ModalBottomSheet(onDismissRequest = { showChatSheet = false }) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "Tanya Penjual • ${item.sellerName}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf(
            "Halo kak, barang ini masih ada?",
            "Bisa COD di ${item.campusCode}?",
            "Bisa bayar lewat QR Code?",
          ).forEach { quick ->
            Surface(
              shape = CircleShape,
              color = MaroonContainer.copy(alpha = 0.6f),
              modifier = Modifier.clip(CircleShape).clickable { onSendInquiry(quick) },
            ) {
              Text(
                text = quick,
                style = MaterialTheme.typography.labelSmall,
                color = MaroonPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }
        }

        if (inquiries.isEmpty()) {
          Text(
            text = "Belum ada pesan. Kirim pertanyaan atau ajak COD di kampus!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp),
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(inquiries, key = { it.id }) { msg ->
              val isMine = msg.senderId == currentUserId
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
              ) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isMine) MaroonPrimary else SurfaceBlush,
                ) {
                  Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                      text = msg.message,
                      style = MaterialTheme.typography.bodyMedium,
                      color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                      text = formatTimestamp(msg.createdAt),
                      style = MaterialTheme.typography.labelSmall,
                      color =
                        if (isMine) Color.White.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = msgText,
            onValueChange = { msgText = it },
            placeholder = { Text("Tulis pesan ke penjual...") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            maxLines = 2,
          )
          IconButton(
            onClick = {
              if (msgText.isNotBlank()) {
                onSendInquiry(msgText)
                msgText = ""
              }
            },
            modifier = Modifier.size(48.dp),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Kirim pesan",
              tint = MaroonPrimary,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PaymentMethodOptionCard(
  method: PaymentMethod,
  icon: ImageVector,
  selected: Boolean,
  onSelect: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = if (selected) SurfaceBlush else MaterialTheme.colorScheme.surface
      ),
    modifier =
      Modifier
        .fillMaxWidth()
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = if (selected) MaroonPrimary else OutlineSoft,
          shape = RoundedCornerShape(14.dp),
        )
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onSelect)
        .testTag("payment_method_${method.code}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      RadioButton(
        selected = selected,
        onClick = onSelect,
        colors = RadioButtonDefaults.colors(selectedColor = MaroonPrimary),
      )
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaroonPrimary,
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = method.label,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = method.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
