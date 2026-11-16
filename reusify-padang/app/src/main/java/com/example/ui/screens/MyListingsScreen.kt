package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ItemStatus
import com.example.data.model.MarketItem
import com.example.ui.components.CampusBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MarketItemImage
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OutlineSoft
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
  items: List<MarketItem>,
  onBack: () -> Unit,
  onAddNew: () -> Unit,
  onUpdateDetails: (MarketItem, String, Long, String) -> Unit,
  onToggleStatus: (MarketItem, String) -> Unit,
  onDelete: (String) -> Unit,
) {
  var editingItem by remember { mutableStateOf<MarketItem?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Kelola Barang Jualan Saya",
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaroonPrimary),
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onAddNew,
        containerColor = MaroonPrimary,
        contentColor = Color.White,
      ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "Tambah Barang")
      }
    },
  ) { padding ->
    if (items.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        EmptyStateView(
          icon = Icons.Filled.Inventory2,
          title = "Belum Ada Barang Dijual",
          subtitle = "Tekan tombol + untuk mulai menjual barang bekas kuliah atau perlengkapan kos Anda.",
        )
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(padding),
      ) {
        items(items, key = { it.id }) { item ->
          MyListingCard(
            item = item,
            onEdit = { editingItem = item },
            onToggleStatus = {
              val next =
                if (item.status == ItemStatus.AVAILABLE) ItemStatus.ARCHIVED
                else ItemStatus.AVAILABLE
              onToggleStatus(item, next)
            },
            onDelete = { onDelete(item.id) },
          )
        }
      }
    }
  }

  editingItem?.let { item ->
    var title by remember { mutableStateOf(item.title) }
    var priceText by remember { mutableStateOf(item.price.toString()) }
    var desc by remember { mutableStateOf(item.description) }

    AlertDialog(
      onDismissRequest = { editingItem = null },
      title = { Text("Edit Barang Jualan", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nama Barang") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = priceText,
            onValueChange = { if (it.all { c -> c.isDigit() }) priceText = it },
            label = { Text("Harga (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Deskripsi") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val newPrice = priceText.toLongOrNull() ?: item.price
            if (title.trim().length >= 3 && newPrice > 0 && desc.trim().length >= 5) {
              onUpdateDetails(item, title, newPrice, desc)
              editingItem = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
        ) {
          Text("Simpan")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingItem = null }) { Text("Batal") }
      },
    )
  }
}

@Composable
private fun MyListingCard(
  item: MarketItem,
  onEdit: () -> Unit,
  onToggleStatus: () -> Unit,
  onDelete: () -> Unit,
) {
  val (statusLabel, statusBg, statusFg) =
    when (item.status) {
      ItemStatus.AVAILABLE -> Triple("Aktif Dijual", SuccessGreenContainer, SuccessGreen)
      ItemStatus.SOLD -> Triple("Terjual", WarningAmberContainer, WarningAmber)
      else -> Triple("Diarsipkan", SurfaceMuted, MaterialTheme.colorScheme.onSurfaceVariant)
    }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth().border(1.dp, OutlineSoft, RoundedCornerShape(16.dp)),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        MarketItemImage(
          imageUrl = item.imageUrl,
          contentDescription = item.title,
          modifier = Modifier.size(68.dp).clip(RoundedCornerShape(12.dp)),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            CampusBadge(campusCode = item.campusCode)
            Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
              Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = statusFg,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
              )
            }
          }
          Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = formatRupiah(item.price),
            style = MaterialTheme.typography.titleMedium,
            color = MaroonPrimary,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      if (item.status != ItemStatus.SOLD) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = onEdit, modifier = Modifier.testTag("edit_listing_${item.id}")) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Edit")
          }
          TextButton(onClick = onToggleStatus) {
            Icon(
              imageVector =
                if (item.status == ItemStatus.AVAILABLE) Icons.Filled.Archive
                else Icons.Filled.Unarchive,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(if (item.status == ItemStatus.AVAILABLE) "Arsipkan" else "Aktifkan")
          }
          TextButton(
            onClick = onDelete,
            colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed),
          ) {
            Icon(
              Icons.Filled.DeleteOutline,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text("Hapus")
          }
        }
      }
    }
  }
}
