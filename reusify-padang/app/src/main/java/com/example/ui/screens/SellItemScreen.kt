package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CatalogSeedData
import com.example.data.model.Campus
import com.example.data.model.PremiumMembershipInfo
import com.example.data.model.SellItemDraft
import com.example.ui.components.MarketItemImage
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.OnOrangeContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.OutlineWarm
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.SurfaceSoftWarm
import com.example.ui.util.MAX_ITEM_IMAGES
import com.example.ui.util.compressMultipleUrisToDataUris
import com.example.ui.util.encodeImageUrls
import com.example.ui.util.formatDateShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellItemScreen(
  campuses: List<Campus>,
  defaultCampusCode: String,
  draft: SellItemDraft,
  onUpdateDraft: ((SellItemDraft) -> SellItemDraft) -> Unit,
  membershipInfo: PremiumMembershipInfo,
  isBusy: Boolean,
  onBack: () -> Unit,
  onOpenPremium: () -> Unit = {},
  onPublish: (
    title: String,
    category: String,
    campus: Campus,
    price: Long,
    description: String,
    imageUrl: String,
    isFeatured: Boolean,
  ) -> Unit,
) {
  val isPremiumUser = membershipInfo.isActive
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val categoryOptions = remember { CatalogSeedData.categories.filter { it != "Semua" } }
  val selectedCategory =
    draft.category.takeIf { it.isNotBlank() && it in categoryOptions } ?: categoryOptions.first()
  val selectedCampus =
    remember(campuses, draft.campusCode, defaultCampusCode) {
      val preferredCode = draft.campusCode.ifBlank { defaultCampusCode }
      campuses.firstOrNull { it.code.equals(preferredCode, ignoreCase = true) }
        ?: campuses.firstOrNull()
        ?: CatalogSeedData.defaultCampuses.first()
    }

  var formError by remember { mutableStateOf<String?>(null) }
  var categoryExpanded by remember { mutableStateOf(false) }
  var campusExpanded by remember { mutableStateOf(false) }
  var isCompressingImages by remember { mutableStateOf(false) }
  var urlInputText by remember {
    mutableStateOf(
      draft.imageUrls.filterNot { it.startsWith("data:image") }.joinToString("\n")
    )
  }

  LaunchedEffect(isPremiumUser) {
    if (isPremiumUser && !draft.hasTouchedFeaturedToggle) {
      onUpdateDraft { it.copy(isFeatured = true) }
    }
  }

  // Zero-permission Android Multi-Photo Picker (Google Play Policy compliant)
  val photoPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickMultipleVisualMedia(MAX_ITEM_IMAGES),
      onResult = { uris ->
        if (uris.isNotEmpty()) {
          scope.launch {
            isCompressingImages = true
            val updatedImages =
              withContext(Dispatchers.IO) {
                compressMultipleUrisToDataUris(
                  context = context,
                  uris = uris,
                  existingImages = draft.imageUrls,
                )
              }
            onUpdateDraft { it.copy(imageUrls = updatedImages) }
            isCompressingImages = false
          }
        }
      },
    )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Jual Barang Bekas",
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("sell_back_button")) {
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
            .widthIn(max = 600.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Foto Barang (${draft.imageUrls.size}/$MAX_ITEM_IMAGES)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Bisa pilih beberapa foto & digeser ke samping",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          TextButton(
            onClick = { onUpdateDraft { it.copy(useUrlInput = !it.useUrlInput) } }
          ) {
            Icon(
              imageVector = Icons.Filled.Link,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = CrimsonPrimary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (draft.useUrlInput) "Pilih dari Galeri" else "Pakai Link Foto",
              color = CrimsonPrimary,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }

        if (draft.useUrlInput) {
          OutlinedTextField(
            value = urlInputText,
            onValueChange = { value ->
              urlInputText = value
              val parsed =
                value
                  .lines()
                  .map { it.trim() }
                  .filter { it.isNotBlank() }
                  .distinct()
                  .take(MAX_ITEM_IMAGES)
              onUpdateDraft { it.copy(imageUrls = parsed) }
            },
            label = { Text("URL Foto Barang (1 link per baris, maks $MAX_ITEM_IMAGES)") },
            placeholder = { Text("https://...\nhttps://...") },
            minLines = 2,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("sell_url_input"),
          )
        } else {
          if (draft.imageUrls.isNotEmpty()) {
            val previewPagerState = rememberPagerState(pageCount = { draft.imageUrls.size })
            LaunchedEffect(draft.imageUrls.size) {
              if (previewPagerState.currentPage >= draft.imageUrls.size && draft.imageUrls.isNotEmpty()) {
                previewPagerState.scrollToPage(draft.imageUrls.size - 1)
              }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceSoftWarm,
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .border(1.5.dp, CrimsonPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("pick_item_photo_box"),
              ) {
                Box(modifier = Modifier.fillMaxSize()) {
                  HorizontalPager(
                    state = previewPagerState,
                    modifier = Modifier.fillMaxSize().testTag("sell_photo_pager"),
                  ) { page ->
                    MarketItemImage(
                      imageUrl = draft.imageUrls[page],
                      contentDescription = "Preview Foto ${page + 1}",
                      modifier = Modifier.fillMaxSize(),
                    )
                  }

                  Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
                  ) {
                    val idx = previewPagerState.currentPage
                    Text(
                      text =
                        if (idx == 0) "Foto 1/${draft.imageUrls.size} (Sampul)"
                        else "Foto ${idx + 1}/${draft.imageUrls.size}",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                  }

                  IconButton(
                    onClick = {
                      val removeIdx = previewPagerState.currentPage
                      onUpdateDraft { current ->
                        current.copy(
                          imageUrls = current.imageUrls.filterIndexed { index, _ -> index != removeIdx }
                        )
                      }
                    },
                    modifier =
                      Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(48.dp)
                        .testTag("sell_remove_photo_button"),
                  ) {
                    Surface(
                      shape = CircleShape,
                      color = Color.Black.copy(alpha = 0.65f),
                      modifier = Modifier.size(30.dp),
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.Filled.Close,
                          contentDescription = "Hapus foto ini",
                          tint = Color.White,
                          modifier = Modifier.size(16.dp),
                        )
                      }
                    }
                  }
                }
              }

              // Thumbnail strip + Add More button
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                itemsIndexed(draft.imageUrls) { idx, img ->
                  val isCurrent = previewPagerState.currentPage == idx
                  Box(
                    modifier =
                      Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                          width = if (isCurrent) 2.dp else 1.dp,
                          color = if (isCurrent) CrimsonPrimary else OutlineWarm,
                          shape = RoundedCornerShape(10.dp),
                        )
                        .clickable {
                          scope.launch { previewPagerState.animateScrollToPage(idx) }
                        }
                  ) {
                    MarketItemImage(
                      imageUrl = img,
                      contentDescription = "Thumbnail foto ${idx + 1}",
                      modifier = Modifier.fillMaxSize(),
                    )
                  }
                }

                if (draft.imageUrls.size < MAX_ITEM_IMAGES) {
                  item {
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = SurfaceSoftWarm,
                      modifier =
                        Modifier
                          .size(58.dp)
                          .border(1.dp, CrimsonPrimary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                          .clip(RoundedCornerShape(10.dp))
                          .clickable(enabled = !isCompressingImages) {
                            photoPickerLauncher.launch(
                              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                          }
                          .testTag("sell_add_more_photo_button"),
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        if (isCompressingImages) {
                          CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CrimsonPrimary,
                            strokeWidth = 2.dp,
                          )
                        } else {
                          Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                              imageVector = Icons.Filled.Add,
                              contentDescription = "Tambah foto lagi",
                              tint = CrimsonPrimary,
                              modifier = Modifier.size(20.dp),
                            )
                            Text(
                              text = "Tambah",
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
            }
          } else {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = SurfaceSoftWarm,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .height(164.dp)
                  .border(1.5.dp, CrimsonPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                  .clip(RoundedCornerShape(16.dp))
                  .clickable(enabled = !isCompressingImages) {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  }
                  .testTag("pick_item_photo_box"),
            ) {
              Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
              ) {
                if (isCompressingImages) {
                  CircularProgressIndicator(color = CrimsonPrimary)
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Memproses foto...",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonPrimary,
                  )
                } else {
                  Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = "Tambah Foto Barang",
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(40.dp),
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Tambah Foto Barang (Maks $MAX_ITEM_IMAGES)",
                    style = MaterialTheme.typography.titleSmall,
                    color = CrimsonPrimary,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = "Pilih sekaligus hingga $MAX_ITEM_IMAGES foto JPG/PNG",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }

        OutlinedTextField(
          value = draft.title,
          onValueChange = { value ->
            onUpdateDraft { it.copy(title = value) }
            formError = null
          },
          label = { Text("Nama Barang") },
          placeholder = { Text("Contoh: Buku Algoritma & Pemrograman") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Filled.ShoppingBag,
              contentDescription = null,
              tint = CrimsonPrimary,
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("item_title_input"),
        )

        // Category & Campus side-by-side matching Screenshot 3
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.weight(1f),
          ) {
            OutlinedTextField(
              value = selectedCategory,
              onValueChange = {},
              readOnly = true,
              label = { Text("Kategori") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            )
            ExposedDropdownMenu(
              expanded = categoryExpanded,
              onDismissRequest = { categoryExpanded = false },
            ) {
              categoryOptions.forEach { cat ->
                DropdownMenuItem(
                  text = { Text(cat) },
                  onClick = {
                    onUpdateDraft { it.copy(category = cat) }
                    categoryExpanded = false
                  },
                )
              }
            }
          }

          ExposedDropdownMenuBox(
            expanded = campusExpanded,
            onExpandedChange = { campusExpanded = !campusExpanded },
            modifier = Modifier.weight(1f),
          ) {
            OutlinedTextField(
              value = selectedCampus.code,
              onValueChange = {},
              readOnly = true,
              label = { Text("Lokasi Kampus") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = campusExpanded) },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            )
            ExposedDropdownMenu(
              expanded = campusExpanded,
              onDismissRequest = { campusExpanded = false },
            ) {
              campuses.forEach { campus ->
                DropdownMenuItem(
                  text = { Text("${campus.code} • ${campus.name}") },
                  onClick = {
                    onUpdateDraft { it.copy(campusCode = campus.code) }
                    campusExpanded = false
                  },
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = draft.priceText,
          onValueChange = { input ->
            if (input.all { it.isDigit() } && input.length <= 9) {
              onUpdateDraft { it.copy(priceText = input) }
              formError = null
            }
          },
          label = { Text("Harga (Rp)") },
          placeholder = { Text("50000") },
          prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = CrimsonPrimary) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("item_price_input"),
        )

        OutlinedTextField(
          value = draft.description,
          onValueChange = { value ->
            onUpdateDraft { it.copy(description = value) }
            formError = null
          },
          label = { Text("Deskripsi & Kondisi Barang") },
          placeholder = {
            Text("Jelaskan kondisi barang, kelengkapan, minus, dan lokasi titik temu COD...")
          },
          minLines = 3,
          maxLines = 5,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("item_description_input"),
        )

        // Premium Feature Highlight Toggle (Membership Rp 15.000 Biaya Admin)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isPremiumUser) OrangeContainer else SurfaceSoftWarm,
          modifier =
            Modifier
              .fillMaxWidth()
              .border(
                width = 1.dp,
                color = if (isPremiumUser) OrangeDeep.copy(alpha = 0.55f) else OutlineWarm,
                shape = RoundedCornerShape(14.dp),
              )
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                if (isPremiumUser) {
                  onUpdateDraft {
                    it.copy(isFeatured = !it.isFeatured, hasTouchedFeaturedToggle = true)
                  }
                } else {
                  onOpenPremium()
                }
              },
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Star,
                  contentDescription = null,
                  tint = if (isPremiumUser) OrangeDeep else CrimsonPrimary,
                )
                Column {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Text(
                      text = "Sorot Iklan (Membership Rp 15.000)",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (isPremiumUser) OnOrangeContainer else MaterialTheme.colorScheme.onSurface,
                    )
                    if (isPremiumUser) {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SuccessGreenContainer,
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                          Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(12.dp),
                          )
                          Text(
                            text = "AKTIF ${membershipInfo.daysRemaining} HR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                          )
                        }
                      }
                    }
                  }
                  Text(
                    text =
                      when {
                        isPremiumUser && membershipInfo.expiresAt != null ->
                          "Membership 1 bulan aktif s.d. ${formatDateShort(membershipInfo.expiresAt)}. Aktifkan untuk sorot teratas."
                        isPremiumUser ->
                          "Membership 1 bulan terverifikasi! Aktifkan untuk tampil di urutan atas beranda."
                        membershipInfo.isExpired ->
                          "Masa aktif 1 bulan habis. Bayar Rp 15.000 untuk perpanjang 1 bulan."
                        else ->
                          "Bayar biaya admin Rp 15.000 (masa aktif 1 bulan) untuk Membership Premium"
                      },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              Switch(
                checked = isPremiumUser && draft.isFeatured,
                onCheckedChange = { checked ->
                  if (isPremiumUser) {
                    onUpdateDraft {
                      it.copy(isFeatured = checked, hasTouchedFeaturedToggle = true)
                    }
                  } else {
                    onOpenPremium()
                  }
                },
                colors =
                  SwitchDefaults.colors(
                    checkedTrackColor = OrangeDeep,
                    uncheckedTrackColor = OutlineWarm,
                  ),
                modifier = Modifier.testTag("sell_featured_switch"),
              )
            }
            if (!isPremiumUser) {
              TextButton(
                onClick = onOpenPremium,
                modifier = Modifier.align(Alignment.End),
              ) {
                Text(
                  text = "Bayar Biaya Admin Rp 15.000 ->",
                  color = OrangeDeep,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }

        if (formError != null) {
          Text(
            text = formError!!,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }

        Button(
          onClick = {
            val priceVal = draft.priceText.toLongOrNull() ?: 0L
            when {
              draft.title.trim().length < 3 -> formError = "Nama barang minimal 3 karakter."
              priceVal <= 0L -> formError = "Masukkan harga barang yang valid (> 0)."
              draft.description.trim().length < 5 -> formError = "Deskripsi kondisi barang minimal 5 karakter."
              else -> {
                onPublish(
                  draft.title.trim(),
                  selectedCategory,
                  selectedCampus,
                  priceVal,
                  draft.description.trim(),
                  encodeImageUrls(draft.imageUrls),
                  isPremiumUser && draft.isFeatured,
                )
              }
            }
          },
          enabled = !isBusy && !isCompressingImages,
          modifier = Modifier.fillMaxWidth().height(54.dp).testTag("publish_item_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
        ) {
          if (isBusy) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = Color.White,
              strokeWidth = 2.dp,
            )
          } else {
            Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Pasang Iklan Barang",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}
