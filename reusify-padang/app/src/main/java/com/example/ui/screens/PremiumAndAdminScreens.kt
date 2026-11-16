package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.Campus
import com.example.data.model.ItemStatus
import com.example.data.model.MarketItem
import com.example.data.model.MarketTransaction
import com.example.data.model.PremiumMembershipInfo
import com.example.data.model.PremiumRequest
import com.example.data.model.TransactionStatus
import com.example.data.model.UserProfile
import com.example.ui.components.CampusBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PremiumBadge
import com.example.ui.components.TransactionStatusBadge
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnOrangeContainer
import com.example.ui.theme.OrangeAccent
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
import java.util.UUID
import kotlinx.coroutines.launch

private const val MEMBERSHIP_ADMIN_FEE_RUPIAH = 15_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
  profile: UserProfile?,
  membershipInfo: PremiumMembershipInfo,
  myRequests: List<PremiumRequest>,
  isBusy: Boolean,
  onBack: () -> Unit,
  onSubmitRequest: (String, String) -> Unit,
  onDownloadMessage: (String) -> Unit = {},
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val paymentCode = remember(profile?.userId, myRequests.size) {
    "ADM-${UUID.randomUUID().toString().take(6).uppercase()}"
  }
  val qrContent = remember(paymentCode) {
    "REUSIFY-MEMBERSHIP|ADMIN_FEE:15000|DURATION:1_BULAN|CODE:$paymentCode"
  }
  val qrBitmap = remember(qrContent) {
    generateQrBitmap(qrContent)
  }
  val hasPending = myRequests.any { it.status == "pending" }
  var paymentConfirmed by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Membership 1 Bulan • Rp 15.000",
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = Color.White,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CrimsonPrimary),
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(padding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = OrangeContainer),
          modifier = Modifier.border(1.dp, OrangeAccent.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
        ) {
          Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = OrangeDeep)
                Text(
                  text =
                    if (membershipInfo.isActive) "Membership 1 Bulan Aktif!"
                    else "Membership Reusify Padang",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = OnOrangeContainer,
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = CrimsonPrimary,
              ) {
                Text(
                  text = "Rp 15.000 / Bln",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
            Text(
              "• Biaya Admin Membership: Rp 15.000 untuk masa aktif 1 bulan (30 hari)",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = OnOrangeContainer,
            )
            Text(
              "• Sorot iklan barang jualan Anda di urutan paling atas katalog selama 1 bulan",
              style = MaterialTheme.typography.bodySmall,
              color = OnOrangeContainer,
            )
            Text(
              "• Lencana Penjual Terverifikasi Membership di setiap produk Anda",
              style = MaterialTheme.typography.bodySmall,
              color = OnOrangeContainer,
            )
            Text(
              "• Uang Rp 15.000 masuk ke Admin & tercatat otomatis di Riwayat (History)",
              style = MaterialTheme.typography.bodySmall,
              color = OnOrangeContainer,
            )
          }
        }

        if (membershipInfo.isExpired) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = WarningAmberContainer),
            modifier = Modifier.fillMaxWidth().border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
          ) {
            Text(
              text = "Masa aktif Membership 1 bulan Anda telah berakhir pada ${formatDateShort(membershipInfo.expiresAt)}. Bayar Rp 15.000 melalui QR Code di bawah untuk perpanjang 1 bulan lagi.",
              style = MaterialTheme.typography.bodySmall,
              color = WarningAmber,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(14.dp),
            )
          }
        }

        if (membershipInfo.isActive) {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SuccessGreenContainer),
            modifier = Modifier.fillMaxWidth().border(1.dp, SuccessGreen.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(18.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(40.dp),
              )
              Text(
                text = "Membership 1 Bulan Anda Aktif!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen,
                textAlign = TextAlign.Center,
              )
              if (membershipInfo.expiresAt != null) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color.White,
                ) {
                  Text(
                    text = "Berlaku s.d. ${formatDateShort(membershipInfo.expiresAt)} (${membershipInfo.daysRemaining} hari tersisa)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  )
                }
              }
              Text(
                text = "Pembayaran biaya admin Rp 15.000 sudah masuk ke Admin dan tercatat di Riwayat. Sekarang tombol Sorot Iklan pada menu Jual Barang sudah aktif.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
              )
              Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
              ) {
                Text("Kembali & Aktifkan Sorot Iklan", fontWeight = FontWeight.Bold)
              }
            }
          }
        } else {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
            modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(18.dp)),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(18.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "Pembayaran Uang Admin (Masa Aktif 1 Bulan)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
              )
              Text(
                text = formatRupiah(MEMBERSHIP_ADMIN_FEE_RUPIAH),
                style = MaterialTheme.typography.headlineLarge,
                color = CrimsonPrimary,
                fontWeight = FontWeight.Bold,
              )
              if (qrBitmap != null) {
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = Color.White,
                  shadowElevation = 2.dp,
                  modifier = Modifier.border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
                ) {
                  Image(
                    bitmap = qrBitmap,
                    contentDescription = "QRIS Biaya Admin Membership Rp 15.000",
                    modifier = Modifier.size(185.dp).padding(10.dp),
                  )
                }
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = CrimsonContainer,
              ) {
                Text(
                  text = "Kode Pembayaran Admin: $paymentCode",
                  style = MaterialTheme.typography.labelLarge,
                  color = CrimsonPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
              }
              OutlinedButton(
                onClick = {
                  scope.launch {
                    val res =
                      saveQrToGallery(
                        context = context,
                        qrContent = qrContent,
                        fileNamePrefix = "QR_Membership_15k",
                        title = "Membership Premium 1 Bulan",
                        code = paymentCode,
                        amountText = "Rp 15.000",
                        subtitle = "Biaya Admin Membership 1 Bulan",
                      )
                    res.fold(
                      onSuccess = { path -> onDownloadMessage("QR Pembayaran 15k diunduh ke Galeri ($path)") },
                      onFailure = { err -> onDownloadMessage("Gagal mengunduh QR: ${err.localizedMessage}") },
                    )
                  }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeDeep),
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("download_membership_qr_button"),
              ) {
                Icon(Icons.Filled.Download, contentDescription = "Unduh QR", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Unduh QR Pembayaran (PNG)", fontWeight = FontWeight.Bold, color = CrimsonPrimary)
              }
              Text(
                text = "Bayar Rp 15.000 untuk masa aktif 1 bulan melalui QR Code di atas, lalu centang konfirmasi agar Admin memverifikasi & mencatatnya ke riwayat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )

              if (!hasPending) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Checkbox(
                    checked = paymentConfirmed,
                    onCheckedChange = { paymentConfirmed = it },
                    colors = CheckboxDefaults.colors(checkedColor = OrangeDeep),
                    modifier = Modifier.testTag("membership_paid_checkbox"),
                  )
                  Text(
                    text = "Saya sudah membayar biaya admin Rp 15.000 (1 Bulan) via QR Code",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                  )
                }
              }

              Button(
                onClick = {
                  onSubmitRequest("Membership Premium 1 Bulan (Rp 15.000)", paymentCode)
                  paymentConfirmed = false
                },
                enabled = !isBusy && !hasPending && paymentConfirmed,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_premium_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeDeep),
              ) {
                Icon(Icons.Filled.QrCode2, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                  text =
                    if (hasPending) "Menunggu Verifikasi Admin (Rp 15.000)"
                    else "Ajukan Aktivasi 1 Bulan (Rp 15.000)",
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }

        if (myRequests.isNotEmpty()) {
          Text(
            text = "Riwayat Pembayaran Membership",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          myRequests.forEach { req ->
            val reqExpiry =
              if (req.status == "approved") calculateOneMonthExpiry(req.updatedAt ?: req.createdAt)
              else null
            val reqQr =
              "REUSIFY-MEMBERSHIP|ADMIN_FEE:15000|DURATION:1_BULAN|CODE:${req.paymentCode}|USER:${req.userName}"
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
              modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Text(req.planName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                  Text(
                    text =
                      if (reqExpiry != null)
                        "Kode: ${req.paymentCode} • s.d. ${formatDateShort(reqExpiry)}"
                      else "Kode: ${req.paymentCode} • 1 Bulan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                IconButton(
                  onClick = {
                    scope.launch {
                      val res =
                        saveQrToGallery(
                          context = context,
                          qrContent = reqQr,
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
                ) {
                  Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Unduh QR ${req.paymentCode}",
                    tint = OrangeDeep,
                  )
                }
                val (label, bg, fg) =
                  when (req.status) {
                    "approved" -> Triple("Aktif 1 Bln", SuccessGreenContainer, SuccessGreen)
                    "rejected" -> Triple("Ditolak", Color(0xFFFFEBEE), ErrorRed)
                    else -> Triple("Cek Admin", WarningAmberContainer, WarningAmber)
                  }
                Surface(shape = RoundedCornerShape(8.dp), color = bg) {
                  Text(
                    text = label,
                    color = fg,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
  campuses: List<Campus>,
  premiumRequests: List<PremiumRequest>,
  users: List<UserProfile>,
  transactions: List<MarketTransaction>,
  items: List<MarketItem>,
  onBack: () -> Unit,
  onSaveCampus: (String?, String, String, String, Boolean) -> Unit,
  onDeleteCampus: (String) -> Unit,
  onReviewPremiumRequest: (PremiumRequest, Boolean) -> Unit,
  onToggleUserPremium: (UserProfile, Boolean) -> Unit,
  onToggleUserVerified: (UserProfile, Boolean) -> Unit,
  onUpdateTransactionStatus: (String, String) -> Unit,
  onModerateItem: (MarketItem, Boolean, String) -> Unit,
  onDeleteItem: (String) -> Unit,
  onDownloadMessage: (String) -> Unit = {},
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var selectedTab by remember { mutableIntStateOf(0) }
  var editingCampus by remember { mutableStateOf<Campus?>(null) }
  var showAddCampusDialog by remember { mutableStateOf(false) }

  val pendingPremiumCount = premiumRequests.count { it.status == "pending" }
  val pendingTxCount =
    transactions.count {
      it.status == TransactionStatus.PENDING_PAYMENT || it.status == TransactionStatus.PAID
    }
  val tabs =
    listOf(
      if (pendingPremiumCount > 0) "Membership 15k ($pendingPremiumCount)" else "Membership 15k",
      if (pendingTxCount > 0) "Transaksi ($pendingTxCount)" else "Transaksi",
      "Iklan (${items.size})",
      "Kampus (${campuses.size})",
    )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Panel Verifikasi Admin", fontWeight = FontWeight.Bold, color = Color.White)
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = Color.White,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CrimsonPrimary),
      )
    },
    floatingActionButton = {
      if (selectedTab == 3) {
        FloatingActionButton(
          onClick = {
            editingCampus = null
            showAddCampusDialog = true
          },
          containerColor = OrangeDeep,
          contentColor = Color.White,
          modifier = Modifier.testTag("add_campus_button"),
        ) {
          Icon(Icons.Filled.Add, contentDescription = "Tambah Kampus")
        }
      }
    },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 12.dp,
        containerColor = SurfacePureWhite,
        contentColor = CrimsonPrimary,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = OrangeDeep,
          )
        },
      ) {
        tabs.forEachIndexed { idx, title ->
          Tab(
            selected = selectedTab == idx,
            onClick = { selectedTab = idx },
            modifier = Modifier.testTag("admin_tab_$idx"),
            text = {
              Text(
                text = title,
                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
              )
            },
          )
        }
      }

      when (selectedTab) {
        0 -> {
          val approvedCount = premiumRequests.count { it.status == "approved" }
          val totalAdminFees = approvedCount * MEMBERSHIP_ADMIN_FEE_RUPIAH

          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            item {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeContainer),
                modifier = Modifier.fillMaxWidth().border(1.dp, OrangeAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column {
                    Text(
                      text = "Total Uang Admin Membership",
                      style = MaterialTheme.typography.bodySmall,
                      color = OnOrangeContainer,
                    )
                    Text(
                      text = formatRupiah(totalAdminFees),
                      style = MaterialTheme.typography.titleLarge,
                      color = CrimsonPrimary,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text("Member Disetujui", style = MaterialTheme.typography.bodySmall, color = OnOrangeContainer)
                    Text(
                      text = "$approvedCount × Rp 15.000",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = OrangeDeep,
                    )
                  }
                }
              }
            }

            item {
              Text(
                text = "Verifikasi Pembayaran Admin Rp 15.000 ($pendingPremiumCount menunggu)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
            }
            if (premiumRequests.isEmpty()) {
              item {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = SurfaceSoftWarm,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text(
                    text = "Belum ada pengajuan Membership dari mahasiswa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp),
                  )
                }
              }
            } else {
              items(premiumRequests, key = { it.id }) { req ->
                val reqExpiry =
                  if (req.status == "approved") calculateOneMonthExpiry(req.updatedAt ?: req.createdAt)
                  else null
                val reqQr =
                  "REUSIFY-MEMBERSHIP|ADMIN_FEE:15000|DURATION:1_BULAN|CODE:${req.paymentCode}|USER:${req.userName}"
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                  modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
                ) {
                  Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(req.userName, fontWeight = FontWeight.Bold)
                        Text("${req.campusCode} • ${req.paymentCode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                          text =
                            if (reqExpiry != null)
                              "Masuk Admin • Berlaku 1 Bln s.d. ${formatDateShort(reqExpiry)}"
                            else "Paket 1 Bulan • ${formatTimestamp(req.createdAt)}",
                          style = MaterialTheme.typography.labelSmall,
                          color = if (req.status == "approved") SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                      IconButton(
                        onClick = {
                          scope.launch {
                            val res =
                              saveQrToGallery(
                                context = context,
                                qrContent = reqQr,
                                fileNamePrefix = "QR_Admin_15k",
                                title = "Membership 1 Bulan • ${req.userName}",
                                code = req.paymentCode,
                                amountText = "Rp 15.000",
                                subtitle = "Bukti QR Membership Admin 1 Bulan",
                              )
                            res.fold(
                              onSuccess = { path -> onDownloadMessage("QR Pembayaran diunduh ($path)") },
                              onFailure = { err -> onDownloadMessage("Gagal mengunduh QR: ${err.localizedMessage}") },
                            )
                          }
                        }
                      ) {
                        Icon(Icons.Filled.Download, contentDescription = "Unduh QR ${req.paymentCode}", tint = OrangeDeep)
                      }
                      Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = CrimsonContainer) {
                          Text(
                            text = "Rp 15.000",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                          )
                        }
                        Text(
                          text = if (req.status == "approved") "MASUK KAS" else req.status.uppercase(),
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = if (req.status == "approved") SuccessGreen else OrangeDeep,
                        )
                      }
                    }
                    if (req.status == "pending") {
                      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                          onClick = { onReviewPremiumRequest(req, true) },
                          colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                          modifier = Modifier.weight(1f),
                        ) {
                          Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                          Spacer(Modifier.width(4.dp))
                          Text("Terima 15k")
                        }
                        OutlinedButton(
                          onClick = { onReviewPremiumRequest(req, false) },
                          colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                          modifier = Modifier.weight(1f),
                        ) {
                          Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                          Spacer(Modifier.width(4.dp))
                          Text("Tolak")
                        }
                      }
                    }
                  }
                }
              }
            }

            item {
              HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineWarm)
              Text(
                text = "Verifikasi Akun & Membership Mahasiswa (${users.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
            }
            items(users, key = { it.userId }) { user ->
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
              ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                      ) {
                        Text(user.displayName, fontWeight = FontWeight.Bold)
                        if (user.isPremium) {
                          PremiumBadge()
                        }
                      }
                      Text(
                        text = "${user.campusCode} • ${user.email}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = if (user.isVerifiedStudent) SuccessGreenContainer else WarningAmberContainer,
                    ) {
                      Text(
                        text = if (user.isVerifiedStudent) "Mahasiswa Valid" else "Belum Valid",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.isVerifiedStudent) SuccessGreen else WarningAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      )
                    }
                  }
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(
                      modifier = Modifier.weight(1f),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Text("Premium", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                      Switch(
                        checked = user.isPremium,
                        onCheckedChange = { onToggleUserPremium(user, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = OrangeDeep),
                      )
                    }
                    Row(
                      modifier = Modifier.weight(1f),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Text("Terverifikasi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                      Switch(
                        checked = user.isVerifiedStudent,
                        onCheckedChange = { onToggleUserVerified(user, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = SuccessGreen),
                      )
                    }
                  }
                }
              }
            }
          }
        }

        1 -> {
          val completedVolume =
            transactions
              .filter { it.status != TransactionStatus.CANCELLED }
              .sumOf { it.amount }
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            item {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSoftWarm),
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Column {
                    Text("Total Volume Transaksi", style = MaterialTheme.typography.bodySmall)
                    Text(
                      formatRupiah(completedVolume),
                      style = MaterialTheme.typography.titleLarge,
                      color = CrimsonPrimary,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text("Total Order", style = MaterialTheme.typography.bodySmall)
                    Text(
                      "${transactions.size}",
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }
              }
            }
            if (transactions.isEmpty()) {
              item {
                EmptyStateView(
                  icon = Icons.Filled.Verified,
                  title = "Belum Ada Transaksi",
                  subtitle = "Semua transaksi QR Code dan COD antar mahasiswa akan terpantau di sini.",
                )
              }
            } else {
              items(transactions, key = { it.id }) { tx ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                  modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
                ) {
                  Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(tx.transactionCode, fontWeight = FontWeight.Bold, color = CrimsonPrimary)
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                      ) {
                        IconButton(
                          onClick = {
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
                                  subtitle = "Bukti QR Transaksi Kampus",
                                )
                              res.fold(
                                onSuccess = { path -> onDownloadMessage("QR Transaksi diunduh ($path)") },
                                onFailure = { err -> onDownloadMessage("Gagal mengunduh QR: ${err.localizedMessage}") },
                              )
                            }
                          },
                          modifier = Modifier.size(30.dp),
                        ) {
                          Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Unduh QR ${tx.transactionCode}",
                            tint = OrangeDeep,
                            modifier = Modifier.size(18.dp),
                          )
                        }
                        TransactionStatusBadge(status = tx.status)
                      }
                    }
                    Text(tx.itemTitle, fontWeight = FontWeight.SemiBold)
                    Text(
                      "${tx.buyerName} (${tx.buyerCampusCode}) -> ${tx.sellerName} • ${formatRupiah(tx.amount)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                      "${tx.paymentMethod} • ${formatTimestamp(tx.createdAt)}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (tx.status == TransactionStatus.PENDING_PAYMENT || tx.status == TransactionStatus.PAID) {
                      HorizontalDivider(color = OutlineWarm)
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                      ) {
                        if (tx.status == TransactionStatus.PENDING_PAYMENT) {
                          Button(
                            onClick = { onUpdateTransactionStatus(tx.id, TransactionStatus.PAID) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeDeep),
                            modifier = Modifier.weight(1f),
                          ) {
                            Text("Verifikasi Bayar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                          }
                        }
                        Button(
                          onClick = { onUpdateTransactionStatus(tx.id, TransactionStatus.COMPLETED) },
                          colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                          modifier = Modifier.weight(1f),
                        ) {
                          Text("Tandai Selesai", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                          onClick = { onUpdateTransactionStatus(tx.id, TransactionStatus.CANCELLED) },
                          colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        ) {
                          Text("Batal", style = MaterialTheme.typography.labelSmall)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        2 -> {
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            item {
              Text(
                text = "Moderasi & Verifikasi Iklan Mahasiswa (${items.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
            }
            if (items.isEmpty()) {
              item {
                EmptyStateView(
                  icon = Icons.Filled.Inventory2,
                  title = "Belum Ada Iklan Pengguna",
                  subtitle = "Barang bekas yang dipasang penjual mahasiswa akan muncul di sini untuk dimoderasi.",
                )
              }
            } else {
              items(items, key = { it.id }) { item ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                  modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
                ) {
                  Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        CampusBadge(campusCode = item.campusCode)
                        if (item.isFeatured) {
                          PremiumBadge(label = "Sorotan")
                        }
                      }
                      Text(
                        text = item.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color =
                          when (item.status) {
                            ItemStatus.AVAILABLE -> SuccessGreen
                            ItemStatus.SOLD -> OrangeDeep
                            else -> ErrorRed
                          },
                      )
                    }
                    Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                      text = "${item.sellerName} • ${formatRupiah(item.price)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = CrimsonPrimary,
                      fontWeight = FontWeight.SemiBold,
                    )
                    if (item.status != ItemStatus.SOLD) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                      ) {
                        OutlinedButton(
                          onClick = {
                            onModerateItem(item, !item.isFeatured, item.status)
                          },
                          modifier = Modifier.weight(1f),
                        ) {
                          Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = OrangeDeep)
                          Spacer(Modifier.width(4.dp))
                          Text(
                            text = if (item.isFeatured) "Lepas Sorot" else "Sorot Iklan",
                            style = MaterialTheme.typography.labelSmall,
                          )
                        }
                        val nextStatus =
                          if (item.status == ItemStatus.ARCHIVED) ItemStatus.AVAILABLE
                          else ItemStatus.ARCHIVED
                        OutlinedButton(
                          onClick = { onModerateItem(item, item.isFeatured, nextStatus) },
                          modifier = Modifier.weight(1f),
                        ) {
                          Icon(
                            imageVector =
                              if (item.status == ItemStatus.ARCHIVED) Icons.Filled.Visibility
                              else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                          )
                          Spacer(Modifier.width(4.dp))
                          Text(
                            text = if (item.status == ItemStatus.ARCHIVED) "Aktifkan" else "Arsipkan",
                            style = MaterialTheme.typography.labelSmall,
                          )
                        }
                        IconButton(onClick = { onDeleteItem(item.id) }) {
                          Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus iklan", tint = ErrorRed)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        3 -> {
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            item {
              Text(
                text = "Kelola Kampus Padang (${campuses.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
            }
            items(campuses, key = { it.code }) { campus ->
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(Icons.Filled.School, contentDescription = null, tint = CrimsonPrimary)
                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      CampusBadge(campusCode = campus.code)
                      Text(
                        text = if (campus.active) "Aktif" else "Nonaktif",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (campus.active) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                    Text(campus.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                      text = "COD: ${campus.meetupSpot}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  IconButton(
                    onClick = {
                      editingCampus = campus
                      showAddCampusDialog = true
                    }
                  ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit kampus", tint = OrangeDeep)
                  }
                  IconButton(onClick = { onDeleteCampus(campus.id) }) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus kampus", tint = ErrorRed)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showAddCampusDialog) {
    var code by remember(editingCampus) { mutableStateOf(editingCampus?.code ?: "") }
    var name by remember(editingCampus) { mutableStateOf(editingCampus?.name ?: "") }
    var meetupSpot by remember(editingCampus) { mutableStateOf(editingCampus?.meetupSpot ?: "") }
    var active by remember(editingCampus) { mutableStateOf(editingCampus?.active ?: true) }

    AlertDialog(
      onDismissRequest = { showAddCampusDialog = false },
      title = {
        Text(
          text = if (editingCampus == null) "Tambah Kampus Baru" else "Edit Kampus",
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            label = { Text("Kode Kampus (mis. UINIB)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("campus_code_input"),
          )
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap Kampus") },
            placeholder = { Text("Contoh: UIN Imam Bonjol Padang") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("campus_name_input"),
          )
          OutlinedTextField(
            value = meetupSpot,
            onValueChange = { meetupSpot = it },
            label = { Text("Titik Temu COD Aman") },
            placeholder = { Text("Contoh: Gerbang Utama Kampus") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Kampus Aktif di Beranda")
            Switch(
              checked = active,
              onCheckedChange = { active = it },
              colors = SwitchDefaults.colors(checkedTrackColor = CrimsonPrimary),
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (code.trim().length >= 2 && name.trim().length >= 3) {
              onSaveCampus(editingCampus?.id, code, name, meetupSpot, active)
              showAddCampusDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
          modifier = Modifier.testTag("save_campus_button"),
        ) {
          Text("Simpan")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddCampusDialog = false }) { Text("Batal") }
      },
    )
  }
}
