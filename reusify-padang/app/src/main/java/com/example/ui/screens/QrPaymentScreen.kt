package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.MarketTransaction
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionStatus
import com.example.ui.components.CampusBadge
import com.example.ui.components.TransactionStatusBadge
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.OutlineSoft
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceBlush
import com.example.ui.util.formatRupiah
import com.example.ui.util.formatTimestamp
import com.example.ui.util.generateQrBitmap
import com.example.ui.util.saveQrToGallery
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPaymentScreen(
  transaction: MarketTransaction?,
  currentUserId: String,
  isBusy: Boolean,
  onBack: () -> Unit,
  onUpdateStatus: (String) -> Unit,
  onCopySuccess: () -> Unit,
  onDownloadMessage: (String) -> Unit = {},
) {
  val clipboard = LocalClipboardManager.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Pembayaran & Struk QR",
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("qr_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = Color.White,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaroonPrimary),
      )
    }
  ) { padding ->
    if (transaction == null) {
      Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(color = MaroonPrimary)
      }
    } else {
      val qrBitmap = remember(transaction.qrPayload) {
        generateQrBitmap(
          transaction.qrPayload.ifBlank {
            "REUSIFY-PADANG|CODE:${transaction.transactionCode}|AMT:${transaction.amount}"
          }
        )
      }
      val isBuyer = transaction.buyerId == currentUserId
      val isSeller = transaction.sellerId == currentUserId

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
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          // QR Code Card
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier =
              Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineSoft, RoundedCornerShape(20.dp)),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
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
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    tint = MaroonPrimary,
                  )
                  Text(
                    text =
                      if (transaction.paymentMethod == PaymentMethod.QR_CODE.code)
                        "QRIS Reusify Padang"
                      else "QR Verifikasi COD",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                }
                TransactionStatusBadge(status = transaction.status)
              }

              Text(
                text = formatRupiah(transaction.amount),
                style = MaterialTheme.typography.headlineLarge,
                color = MaroonPrimary,
                fontWeight = FontWeight.Bold,
              )

              if (qrBitmap != null) {
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = Color.White,
                  shadowElevation = 2.dp,
                  modifier =
                    Modifier
                      .padding(vertical = 6.dp)
                      .border(1.dp, OutlineSoft, RoundedCornerShape(16.dp)),
                ) {
                  Image(
                    bitmap = qrBitmap,
                    contentDescription = "QR Code Pembayaran ${transaction.transactionCode}",
                    modifier =
                      Modifier
                        .size(210.dp)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("qr_code_image"),
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceBlush,
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = transaction.transactionCode,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaroonPrimary,
                  )
                  IconButton(
                    onClick = {
                      clipboard.setText(AnnotatedString(transaction.transactionCode))
                      onCopySuccess()
                    },
                    modifier = Modifier.size(28.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Filled.ContentCopy,
                      contentDescription = "Salin kode transaksi",
                      tint = MaroonPrimary,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
              }

              Text(
                text =
                  if (transaction.paymentMethod == PaymentMethod.QR_CODE.code)
                    "Scan QR Code di atas melalui e-wallet / mobile banking, lalu konfirmasi pembayaran."
                  else
                    "Tunjukkan QR Code ini saat bertemu penjual di kampus sebagai bukti pesanan COD.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )

              val qrContent =
                transaction.qrPayload.ifBlank {
                  "REUSIFY-PADANG|CODE:${transaction.transactionCode}|AMT:${transaction.amount}"
                }
              OutlinedButton(
                onClick = {
                  scope.launch {
                    val res =
                      saveQrToGallery(
                        context = context,
                        qrContent = qrContent,
                        fileNamePrefix = "QR_Transaksi",
                        title = transaction.itemTitle,
                        code = transaction.transactionCode,
                        amountText = formatRupiah(transaction.amount),
                        subtitle =
                          if (transaction.paymentMethod == PaymentMethod.QR_CODE.code)
                            "QRIS Pembayaran Barang Kampus"
                          else "QR Verifikasi COD Kampus",
                      )
                    res.fold(
                      onSuccess = { path -> onDownloadMessage("QR berhasil diunduh ke Galeri ($path)") },
                      onFailure = { err -> onDownloadMessage("Gagal mengunduh QR: ${err.localizedMessage}") },
                    )
                  }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeDeep),
                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("download_tx_qr_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Download,
                  contentDescription = "Unduh QR Pembayaran",
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Unduh QR Pembayaran (PNG)",
                  fontWeight = FontWeight.Bold,
                  color = CrimsonPrimary,
                )
              }
            }
          }

          // Transaction Details Card
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().border(1.dp, OutlineSoft, RoundedCornerShape(16.dp)),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "Rincian Transaksi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              HorizontalDivider(color = OutlineSoft)
              DetailInfoRow("Barang", transaction.itemTitle)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Kampus",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CampusBadge(campusCode = transaction.campusCode, showIcon = true)
              }
              DetailInfoRow(
                "Metode Bayar",
                if (transaction.paymentMethod == PaymentMethod.QR_CODE.code) "QR Code / QRIS"
                else "COD / Tunai di Kampus",
              )
              DetailInfoRow("Pembeli", "${transaction.buyerName} (${transaction.buyerCampusCode})")
              DetailInfoRow("Penjual", transaction.sellerName)
              DetailInfoRow("Waktu", formatTimestamp(transaction.createdAt))
              if (transaction.meetupNote.isNotBlank()) {
                DetailInfoRow("Catatan COD", transaction.meetupNote)
              }
            }
          }

          // Action Buttons based on role & status
          if (transaction.status == TransactionStatus.PENDING_PAYMENT) {
            if (isBuyer) {
              Button(
                onClick = { onUpdateStatus(TransactionStatus.PAID) },
                enabled = !isBusy,
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_qr_payment_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
              ) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text =
                    if (transaction.paymentMethod == PaymentMethod.QR_CODE.code)
                      "Konfirmasi Sudah Bayar QR"
                    else "Konfirmasi Pembayaran COD",
                  fontWeight = FontWeight.Bold,
                )
              }

              OutlinedButton(
                onClick = { onUpdateStatus(TransactionStatus.CANCELLED) },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
              ) {
                Icon(imageVector = Icons.Filled.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Batalkan Pesanan", fontWeight = FontWeight.SemiBold)
              }
            } else if (isSeller) {
              Button(
                onClick = { onUpdateStatus(TransactionStatus.COMPLETED) },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
              ) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Konfirmasi Pembayaran & Selesai", fontWeight = FontWeight.Bold)
              }
            }
          } else if (transaction.status == TransactionStatus.PAID) {
            if (isBuyer || isSeller) {
              Button(
                onClick = { onUpdateStatus(TransactionStatus.COMPLETED) },
                enabled = !isBusy,
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("complete_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
              ) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text =
                    if (isBuyer) "Barang Diterima • Selesaikan Transaksi"
                    else "Serah Terima Selesai",
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(0.38f),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(0.62f),
    )
  }
}
