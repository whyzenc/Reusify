package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MarketTransaction
import com.example.data.model.PaymentMethod
import com.example.data.model.PremiumMembershipInfo
import com.example.data.model.PremiumRequest
import com.example.data.model.TransactionStatus
import com.example.data.model.UserRole
import com.example.ui.components.CampusBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MarketItemImage
import com.example.ui.components.TransactionStatusBadge
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnOrangeContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.OutlineWarm
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSoftWarm
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.util.calculateOneMonthExpiry
import com.example.ui.util.formatDateShort
import com.example.ui.util.formatRupiah
import com.example.ui.util.formatTimestamp
import com.example.ui.util.generateQrBitmap
import com.example.ui.util.saveQrToGallery
import kotlinx.coroutines.launch

private const val HISTORY_MEMBERSHIP_FEE_RUPIAH = 15_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  buyerTransactions: List<MarketTransaction>,
  sellerTransactions: List<MarketTransaction>,
  myPremiumRequests: List<PremiumRequest> = emptyList(),
  adminPremiumRequests: List<PremiumRequest> = emptyList(),
  membershipInfo: PremiumMembershipInfo = PremiumMembershipInfo(),
  isAdmin: Boolean = false,
  activeRole: UserRole = UserRole.BUYER,
  onBack: () -> Unit,
  onTransactionClick: (MarketTransaction) -> Unit,
  onDownloadMessage: (String) -> Unit = {},
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var selectedTab by remember(activeRole) {
    mutableIntStateOf(if (activeRole == UserRole.SELLER) 1 else 0)
  }
  var qrPreviewRequest by remember { mutableStateOf<PremiumRequest?>(null) }

  val membershipHistoryList =
    if (isAdmin && adminPremiumRequests.isNotEmpty()) adminPremiumRequests else myPremiumRequests

  val tabs = listOf(
    "Pembelian Saya (${buyerTransactions.size})",
    "Penjualan Saya (${sellerTransactions.size})",
    if (isAdmin) "Uang Admin 15k (${adminPremiumRequests.count { it.status == "approved" }})"
    else "Membership 15k (${myPremiumRequests.size})",
  )
  val accentColor =
    when (selectedTab) {
      1 -> OrangeDeep
      else -> CrimsonPrimary
    }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Riwayat & Bukti QR",
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = Color.White,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = accentColor),
      )
    }
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
        tabs.forEachIndexed { idx, title ->
          Tab(
            selected = selectedTab == idx,
            onClick = { selectedTab = idx },
            modifier = Modifier.testTag("history_tab_$idx"),
            text = {
              Text(
                text = title,
                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
              )
            },
          )
        }
      }

      if (selectedTab == 2) {
        val approvedRequests = membershipHistoryList.filter { it.status == "approved" }
        val totalMembershipFees = approvedRequests.size * HISTORY_MEMBERSHIP_FEE_RUPIAH

        if (membershipHistoryList.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyStateView(
              icon = Icons.Filled.Star,
              title = if (isAdmin) "Belum Ada Pembayaran 15k Masuk" else "Belum Ada Riwayat Membership",
              subtitle =
                if (isAdmin)
                  "Pembayaran Membership 1 Bulan Rp 15.000 dari penjual akan tercatat otomatis ke kas Admin di sini."
                else
                  "Biaya admin Membership 1 Bulan Rp 15.000 yang dibayarkan ke Admin akan tercatat lengkap dengan QR-nya di sini.",
            )
          }
        } else {
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            item {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = OrangeContainer,
                modifier = Modifier.fillMaxWidth().border(1.dp, OrangeDeep.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text =
                        if (isAdmin) "Total Uang 15k Masuk ke Admin"
                        else "Total Biaya Membership ke Admin",
                      style = MaterialTheme.typography.bodySmall,
                      color = OnOrangeContainer,
                    )
                    Text(
                      text = formatRupiah(totalMembershipFees),
                      style = MaterialTheme.typography.titleLarge,
                      color = CrimsonPrimary,
                      fontWeight = FontWeight.Bold,
                    )
                    if (!isAdmin && membershipInfo.isActive && membershipInfo.expiresAt != null) {
                      Text(
                        text = "Aktif 1 Bulan • s.d. ${formatDateShort(membershipInfo.expiresAt)} (${membershipInfo.daysRemaining} hari lagi)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                  }
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CrimsonPrimary,
                  ) {
                    Text(
                      text = "${approvedRequests.size} × 15k",
                      style = MaterialTheme.typography.labelMedium,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                  }
                }
              }
            }

            items(membershipHistoryList, key = { it.id }) { req ->
              val expiresAt =
                if (req.status == "approved") calculateOneMonthExpiry(req.updatedAt ?: req.createdAt)
                else null
              val reqQrContent =
                "REUSIFY-MEMBERSHIP|ADMIN_FEE:15000|DURATION:1_BULAN|CODE:${req.paymentCode}|USER:${req.userName}"
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { qrPreviewRequest = req },
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Icon(Icons.Filled.Star, contentDescription = null, tint = OrangeDeep, modifier = Modifier.size(16.dp))
                      Text(
                        text = req.paymentCode,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                      )
                    }
                    val (statusLabel, bg, fg) =
                      when (req.status) {
                        "approved" -> Triple("Masuk ke Admin • Aktif", SuccessGreenContainer, SuccessGreen)
                        "rejected" -> Triple("Ditolak", Color(0xFFFFEBEE), ErrorRed)
                        else -> Triple("Menunggu Admin", WarningAmberContainer, WarningAmber)
                      }
                    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
                      Text(
                        text = statusLabel,
                        color = fg,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      )
                    }
                  }

                  Text(
                    text = "Membership Premium 1 Bulan (${req.userName})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    CampusBadge(campusCode = req.campusCode)
                    Text(
                      text = "Biaya Admin Rp 15.000 • Durasi 1 Bulan",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  Text(
                    text =
                      if (expiresAt != null)
                        "Dibayar: ${formatTimestamp(req.updatedAt ?: req.createdAt)} • Berlaku s.d. ${formatDateShort(expiresAt)}"
                      else "Diajukan: ${formatTimestamp(req.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(
                      text = formatRupiah(HISTORY_MEMBERSHIP_FEE_RUPIAH),
                      style = MaterialTheme.typography.titleMedium,
                      color = CrimsonPrimary,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                      onClick = { qrPreviewRequest = req },
                      shape = RoundedCornerShape(10.dp),
                    ) {
                      Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(4.dp))
                      Text("Lihat QR", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                      onClick = {
                        scope.launch {
                          val res =
                            saveQrToGallery(
                              context = context,
                              qrContent = reqQrContent,
                              fileNamePrefix = "QR_Membership_15k",
                              title = "Membership 1 Bulan • ${req.userName}",
                              code = req.paymentCode,
                              amountText = "Rp 15.000",
                              subtitle = "Biaya Admin Membership 1 Bulan",
                            )
                          res.fold(
                            onSuccess = { path -> onDownloadMessage("QR Membership diunduh ($path)") },
                            onFailure = { err -> onDownloadMessage("Gagal mengunduh QR: ${err.localizedMessage}") },
                          )
                        }
                      },
                      shape = RoundedCornerShape(10.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = OrangeDeep),
                    ) {
                      Icon(Icons.Filled.Download, contentDescription = "Unduh QR", modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(4.dp))
                      Text("Unduh QR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        val activeList = if (selectedTab == 0) buyerTransactions else sellerTransactions
        val isBuyerMode = selectedTab == 0
        val approvedSellerFees =
          myPremiumRequests.count { it.status == "approved" } * HISTORY_MEMBERSHIP_FEE_RUPIAH

        if (activeList.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyStateView(
              icon = Icons.Filled.History,
              title = if (isBuyerMode) "Belum Ada Riwayat Pembelian" else "Belum Ada Riwayat Penjualan",
              subtitle =
                if (isBuyerMode)
                  "Transaksi pembelian barang via QR Code atau COD akan tercatat otomatis di sini."
                else
                  "Saat ada mahasiswa membeli barang jualan Anda, riwayat & QR struk muncul di sini.",
            )
          }
        } else {
          val totalNominal =
            activeList
              .filter { it.status != TransactionStatus.CANCELLED }
              .sumOf { it.amount }

          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            item {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceSoftWarm,
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
              ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column {
                      Text(
                        text = if (isBuyerMode) "Total Belanja Kampus" else "Total Pendapatan Penjualan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                      Text(
                        text = formatRupiah(totalNominal),
                        style = MaterialTheme.typography.titleLarge,
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = CrimsonContainer,
                    ) {
                      Text(
                        text = "${activeList.size} transaksi",
                        style = MaterialTheme.typography.labelMedium,
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      )
                    }
                  }
                  if (!isBuyerMode && approvedSellerFees > 0L) {
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = OrangeContainer,
                      modifier = Modifier.fillMaxWidth().clickable { selectedTab = 2 },
                    ) {
                      Text(
                        text = "Biaya Admin Membership 1 Bulan ke Admin: ${formatRupiah(approvedSellerFees)} (Lihat detail di tab Membership 15k)",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnOrangeContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      )
                    }
                  }
                }
              }
            }

            items(activeList, key = { it.id }) { tx ->
              TransactionHistoryCard(
                tx = tx,
                isBuyerMode = isBuyerMode,
                onClick = { onTransactionClick(tx) },
                onDownloadQr = {
                  val qrContent =
                    tx.qrPayload.ifBlank {
                      "REUSIFY-PADANG|CODE:${tx.transactionCode}|AMT:${tx.amount}"
                    }
                  scope.launch {
                    val res =
                      saveQrToGallery(
                        context = context,
                        qrContent = qrContent,
                        fileNamePrefix = "QR_Transaksi",
                        title = tx.itemTitle,
                        code = tx.transactionCode,
                        amountText = formatRupiah(tx.amount),
                        subtitle =
                          if (tx.paymentMethod == PaymentMethod.QR_CODE.code)
                            "QRIS Pembayaran Barang Kampus"
                          else "QR Verifikasi COD Kampus",
                      )
                    res.fold(
                      onSuccess = { path -> onDownloadMessage("QR berhasil diunduh ($path)") },
                      onFailure = { err -> onDownloadMessage("Gagal mengunduh QR: ${err.localizedMessage}") },
                    )
                  }
                },
              )
            }
          }
        }
      }
    }
  }

  qrPreviewRequest?.let { req ->
    val reqQrContent =
      "REUSIFY-MEMBERSHIP|ADMIN_FEE:15000|DURATION:1_BULAN|CODE:${req.paymentCode}|USER:${req.userName}"
    val bmp = remember(reqQrContent) { generateQrBitmap(reqQrContent) }
    AlertDialog(
      onDismissRequest = { qrPreviewRequest = null },
      title = { Text("QR Biaya Admin Rp 15.000", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          if (bmp != null) {
            Image(
              bitmap = bmp,
              contentDescription = "QR ${req.paymentCode}",
              modifier = Modifier.size(200.dp),
            )
          }
          Text("Kode: ${req.paymentCode}", fontWeight = FontWeight.Bold, color = CrimsonPrimary)
          Text(
            text = "Membership Premium 1 Bulan • Rp 15.000",
            style = MaterialTheme.typography.bodySmall,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            scope.launch {
              val res =
                saveQrToGallery(
                  context = context,
                  qrContent = reqQrContent,
                  fileNamePrefix = "QR_Membership_15k",
                  title = "Membership 1 Bulan • ${req.userName}",
                  code = req.paymentCode,
                  amountText = "Rp 15.000",
                  subtitle = "Biaya Admin Membership 1 Bulan",
                )
              res.fold(
                onSuccess = { path -> onDownloadMessage("QR Membership diunduh ($path)") },
                onFailure = { err -> onDownloadMessage("Gagal mengunduh QR: ${err.localizedMessage}") },
              )
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = OrangeDeep),
        ) {
          Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Unduh QR")
        }
      },
      dismissButton = {
        TextButton(onClick = { qrPreviewRequest = null }) { Text("Tutup") }
      },
    )
  }
}

@Composable
private fun TransactionHistoryCard(
  tx: MarketTransaction,
  isBuyerMode: Boolean,
  onClick: () -> Unit,
  onDownloadQr: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier =
      Modifier
        .fillMaxWidth()
        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag("tx_card_${tx.id}"),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector =
              if (tx.paymentMethod == PaymentMethod.QR_CODE.code) Icons.Filled.QrCode2
              else Icons.Filled.Handshake,
            contentDescription = null,
            tint = CrimsonPrimary,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = tx.transactionCode,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = CrimsonPrimary,
          )
        }
        TransactionStatusBadge(status = tx.status)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        MarketItemImage(
          imageUrl = tx.itemImageUrl,
          contentDescription = tx.itemTitle,
          modifier = Modifier.size(66.dp).clip(RoundedCornerShape(12.dp)),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
          Text(
            text = tx.itemTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            CampusBadge(campusCode = tx.campusCode)
            Text(
              text =
                if (isBuyerMode) "Penjual: ${tx.sellerName}"
                else "Pembeli: ${tx.buyerName}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Text(
            text = formatTimestamp(tx.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = formatRupiah(tx.amount),
          style = MaterialTheme.typography.titleMedium,
          color = CrimsonPrimary,
          fontWeight = FontWeight.Bold,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          IconButton(
            onClick = onDownloadQr,
            modifier = Modifier.size(34.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Download,
              contentDescription = "Unduh QR ${tx.transactionCode}",
              tint = OrangeDeep,
              modifier = Modifier.size(18.dp),
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = CrimsonContainer.copy(alpha = 0.65f),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.ReceiptLong,
                contentDescription = null,
                tint = CrimsonPrimary,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = "Lihat QR & Struk",
                style = MaterialTheme.typography.labelSmall,
                color = CrimsonPrimary,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}
