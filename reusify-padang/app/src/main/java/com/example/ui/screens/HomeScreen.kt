package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CatalogSeedData
import com.example.data.model.Campus
import com.example.data.model.MarketItem
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FeaturedTopProductCard
import com.example.ui.components.ProductGridCard
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.OutlineWarm
import com.example.ui.theme.SurfaceIvory
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSoftWarm
import com.example.ui.viewmodel.UiState

@Composable
fun HomeScreen(
  itemsState: UiState<List<MarketItem>>,
  campuses: List<Campus>,
  activeRole: UserRole,
  currentUserId: String,
  sellerOnlyFilter: Boolean,
  mySellingCount: Int,
  pendingSellerOrdersCount: Int,
  sellerSoldCount: Int,
  searchQuery: String,
  selectedCampusCode: String,
  selectedCategory: String,
  isAdmin: Boolean,
  unreadChatCount: Int = 0,
  unreadSellerChatCount: Int = 0,
  onSearchChange: (String) -> Unit,
  onCampusSelect: (String) -> Unit,
  onCategorySelect: (String) -> Unit,
  onToggleSellerOnly: (Boolean) -> Unit,
  onSwitchRole: (UserRole) -> Unit,
  onItemClick: (MarketItem) -> Unit,
  onSellClick: () -> Unit,
  onManageListingsClick: () -> Unit,
  onChatClick: () -> Unit = {},
  onHistoryClick: () -> Unit,
  onProfileClick: () -> Unit,
  onAdminClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isSeller = activeRole == UserRole.SELLER
  val primaryAccent = if (isSeller) OrangeDeep else CrimsonPrimary
  val secondaryContainer = if (isSeller) OrangeContainer else CrimsonContainer

  val headerBrush =
    if (isSeller) {
      Brush.horizontalGradient(listOf(CrimsonPrimary, OrangeDeep))
    } else {
      Brush.horizontalGradient(listOf(CrimsonDark, CrimsonPrimary, CrimsonLight))
    }

  Box(modifier = modifier.fillMaxSize().background(SurfaceIvory)) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Unified Red-White-Orange Header (distinct gradient & role pill for Buyer vs Seller)
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(headerBrush)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
              text = "Reusify Padang",
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
            // Interactive compact Role Badge (allows seeing current mode & quick switching)
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White.copy(alpha = 0.94f),
              modifier =
                Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable {
                    onSwitchRole(if (isSeller) UserRole.BUYER else UserRole.SELLER)
                  }
                  .testTag("home_role_pill"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector =
                    if (isSeller) Icons.Filled.Storefront else Icons.Filled.ShoppingBag,
                  contentDescription = null,
                  tint = primaryAccent,
                  modifier = Modifier.size(13.dp),
                )
                Text(
                  text = if (isSeller) "Mode Penjual" else "Mode Pembeli",
                  style = MaterialTheme.typography.labelSmall,
                  color = primaryAccent,
                  fontWeight = FontWeight.Bold,
                )
                Icon(
                  imageVector = Icons.Filled.SwapHoriz,
                  contentDescription = "Ganti mode",
                  tint = primaryAccent.copy(alpha = 0.75f),
                  modifier = Modifier.size(13.dp),
                )
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            if (isAdmin) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfacePureWhite,
                modifier =
                  Modifier
                    .padding(end = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAdminClick)
                    .testTag("home_admin_badge"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Panel Admin",
                    tint = OrangeDeep,
                    modifier = Modifier.size(16.dp),
                  )
                  Text(
                    text = "Admin",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OrangeDeep,
                  )
                }
              }
            }
            IconButton(
              onClick = onChatClick,
              modifier = Modifier.testTag("home_chat_button"),
            ) {
              BadgedBox(
                badge = {
                  if (unreadChatCount > 0) {
                    Badge(containerColor = Color.White, contentColor = CrimsonPrimary) {
                      Text(
                        text = if (unreadChatCount > 9) "9+" else unreadChatCount.toString(),
                        fontWeight = FontWeight.Bold,
                      )
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Filled.ChatBubbleOutline,
                  contentDescription = "History Chat & Notifikasi",
                  tint = Color.White,
                )
              }
            }
            IconButton(
              onClick = onHistoryClick,
              modifier = Modifier.testTag("home_history_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.History,
                contentDescription = "Riwayat Transaksi",
                tint = Color.White,
              )
            }
            IconButton(
              onClick = onProfileClick,
              modifier = Modifier.testTag("home_profile_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profil Saya",
                tint = Color.White,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Crisp Pure White Search Bar
        Surface(
          modifier = Modifier.fillMaxWidth().height(46.dp),
          shape = RoundedCornerShape(12.dp),
          color = SurfacePureWhite,
          shadowElevation = 2.dp,
        ) {
          Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Cari barang",
              tint = primaryAccent,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
              if (searchQuery.isEmpty()) {
                Text(
                  text =
                    if (isSeller) "Cari iklan saya atau cek harga pasar..."
                    else "Cari buku, kipas, kalkulator, helm...",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
              }
              BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                singleLine = true,
                textStyle =
                  MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                  ),
                cursorBrush = SolidColor(primaryAccent),
                modifier = Modifier.fillMaxWidth().testTag("home_search_input"),
              )
            }
            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { onSearchChange("") },
                modifier = Modifier.size(28.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Hapus pencarian",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }

      // Campus Filter Chips Row
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        item {
          CampusFilterPill(
            label = "Semua Kampus",
            selected = selectedCampusCode == "ALL",
            activeColor = primaryAccent,
            onClick = { onCampusSelect("ALL") },
          )
        }
        items(campuses, key = { it.code }) { campus ->
          CampusFilterPill(
            label = campus.code,
            selected = selectedCampusCode.equals(campus.code, ignoreCase = true),
            activeColor = primaryAccent,
            onClick = { onCampusSelect(campus.code) },
          )
        }
      }

      // Category Filter Chips Row
      LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(CatalogSeedData.categories) { category ->
          val selected = selectedCategory == category
          Surface(
            shape = CircleShape,
            color = if (selected) secondaryContainer else SurfacePureWhite,
            modifier =
              Modifier
                .border(
                  1.dp,
                  if (selected) primaryAccent else OutlineWarm,
                  CircleShape,
                )
                .clip(CircleShape)
                .clickable { onCategorySelect(category) },
          ) {
            Text(
              text = category,
              style = MaterialTheme.typography.labelMedium,
              color = if (selected) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
        }
      }

      // Marketplace Grid (Same clean 2-column grid for both roles, with tailored top banner)
      when (itemsState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primaryAccent)
          }
        }
        is UiState.Error -> {
          EmptyStateView(
            icon = Icons.Filled.ShoppingBag,
            title = "Gagal Memuat Katalog",
            subtitle = itemsState.message,
          )
        }
        is UiState.Success -> {
          val list = itemsState.data
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 162.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            // Role-Tailored Top Strip ("berbeda namun nyaris sama")
            item(span = { GridItemSpan(maxLineSpan) }) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (unreadChatCount > 0) {
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OrangeContainer,
                    modifier =
                      Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrangeDeep.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onChatClick)
                        .testTag("home_unread_chat_banner"),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
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
                          text = "$unreadChatCount notifikasi chat belum dibaca",
                          style = MaterialTheme.typography.labelLarge,
                          color = CrimsonPrimary,
                          fontWeight = FontWeight.Bold,
                        )
                      }
                      Text(
                        text = "Buka Chat ->",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrangeDeep,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                  }
                }
                if (isSeller) {
                  SellerOverviewStrip(
                    mySellingCount = mySellingCount,
                    pendingOrdersCount = pendingSellerOrdersCount,
                    soldCount = sellerSoldCount,
                    unreadSellerChatCount = unreadSellerChatCount,
                    sellerOnlyFilter = sellerOnlyFilter,
                    onToggleSellerOnly = onToggleSellerOnly,
                    onManageListings = onManageListingsClick,
                    onOpenOrders = onHistoryClick,
                    onOpenChats = onChatClick,
                  )
                } else {
                  BuyerOverviewStrip(
                    itemCount = list.size,
                    selectedCampusCode = selectedCampusCode,
                  )
                }
              }
            }

            if (list.isEmpty()) {
              item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyStateView(
                  icon = if (isSeller) Icons.Filled.Inventory2 else Icons.Filled.ShoppingBag,
                  title =
                    if (isSeller && sellerOnlyFilter) "Etalase Anda Masih Kosong"
                    else "Belum Ada Barang Cocok",
                  subtitle =
                    if (isSeller && sellerOnlyFilter)
                      "Tekan '+ Pasang Iklan' untuk mulai menjual barang bekas kampus Anda."
                    else
                      "Coba ubah filter kampus, kategori, atau kata kunci pencarian.",
                )
              }
            } else {
              val featuredList = list.filter { it.isFeatured }
              val regularList = list.filter { !it.isFeatured }

              if (featuredList.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "header_featured") {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = OrangeDeep,
                        modifier = Modifier.size(18.dp),
                      )
                      Text(
                        text = "Sorotan Premium Teratas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                      )
                    }
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = OrangeContainer,
                    ) {
                      Text(
                        text = "${featuredList.size} Iklan",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangeDeep,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      )
                    }
                  }
                }

                items(
                  featuredList,
                  key = { "featured_${it.id}" },
                  span = { GridItemSpan(maxLineSpan) },
                ) { item ->
                  FeaturedTopProductCard(
                    item = item,
                    isOwnedByViewer = item.sellerId == currentUserId,
                    accentColor = if (isSeller) OrangeDeep else CrimsonPrimary,
                    onClick = { onItemClick(item) },
                  )
                }

                if (regularList.isNotEmpty()) {
                  item(span = { GridItemSpan(maxLineSpan) }, key = "header_regular") {
                    Text(
                      text = "Barang Kampus Lainnya (${regularList.size})",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.padding(top = 6.dp),
                    )
                  }
                }
              }

              items(regularList, key = { it.id }) { item ->
                ProductGridCard(
                  item = item,
                  isOwnedByViewer = item.sellerId == currentUserId,
                  accentColor = if (isSeller) OrangeDeep else CrimsonPrimary,
                  onClick = { onItemClick(item) },
                )
              }
            }
          }
        }
      }
    }

    // Floating Action Button: Buyer -> Pesanan Saya (Red), Seller -> + Pasang Iklan (Orange)
    ExtendedFloatingActionButton(
      onClick = if (isSeller) onSellClick else onHistoryClick,
      containerColor = if (isSeller) OrangeDeep else CrimsonPrimary,
      contentColor = Color.White,
      shape = RoundedCornerShape(16.dp),
      modifier =
        Modifier
          .align(Alignment.BottomEnd)
          .navigationBarsPadding()
          .padding(end = 16.dp, bottom = 16.dp)
          .testTag("sell_item_fab"),
      icon = {
        Icon(
          imageVector = if (isSeller) Icons.Filled.Add else Icons.Filled.ReceiptLong,
          contentDescription = null,
        )
      },
      text = {
        Text(
          text = if (isSeller) "Pasang Iklan" else "Pesanan Saya",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      },
    )
  }
}

@Composable
private fun BuyerOverviewStrip(
  itemCount: Int,
  selectedCampusCode: String,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = SurfacePureWhite,
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(bottom = 2.dp)
        .border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = CrimsonContainer,
        ) {
          Icon(
            imageVector = Icons.Filled.QrCode2,
            contentDescription = null,
            tint = CrimsonPrimary,
            modifier = Modifier.padding(6.dp).size(16.dp),
          )
        }
        Column {
          Text(
            text = "$itemCount barang siap dibeli",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text =
              if (selectedCampusCode == "ALL") "Seluruh Kampus Padang • QRIS & COD"
              else "Kampus $selectedCampusCode • QRIS & COD",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = OrangeContainer,
      ) {
        Text(
          text = "Siap COD",
          style = MaterialTheme.typography.labelSmall,
          color = OrangeDeep,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun SellerOverviewStrip(
  mySellingCount: Int,
  pendingOrdersCount: Int,
  soldCount: Int,
  unreadSellerChatCount: Int,
  sellerOnlyFilter: Boolean,
  onToggleSellerOnly: (Boolean) -> Unit,
  onManageListings: () -> Unit,
  onOpenOrders: () -> Unit,
  onOpenChats: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = SurfacePureWhite,
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(bottom = 2.dp)
        .border(1.dp, OrangeAccent.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
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
            imageVector = Icons.Filled.Storefront,
            contentDescription = null,
            tint = OrangeDeep,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Ringkasan Toko Penjual",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
        }
        Text(
          text = "Kelola Etalase",
          style = MaterialTheme.typography.labelMedium,
          color = CrimsonPrimary,
          fontWeight = FontWeight.Bold,
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable(onClick = onManageListings)
              .padding(horizontal = 6.dp, vertical = 2.dp),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SellerMiniStat(
          value = mySellingCount.toString(),
          label = "Aktif",
          bgColor = SurfaceSoftWarm,
          valueColor = OrangeDeep,
          onClick = onManageListings,
          modifier = Modifier.weight(1f),
        )
        SellerMiniStat(
          value = pendingOrdersCount.toString(),
          label = "Order",
          bgColor = CrimsonContainer.copy(alpha = 0.7f),
          valueColor = CrimsonPrimary,
          onClick = onOpenOrders,
          modifier = Modifier.weight(1f),
        )
        SellerMiniStat(
          value = unreadSellerChatCount.toString(),
          label = "Chat Baru",
          bgColor = OrangeContainer.copy(alpha = 0.85f),
          valueColor = if (unreadSellerChatCount > 0) CrimsonPrimary else OrangeDeep,
          onClick = onOpenChats,
          modifier = Modifier.weight(1f),
        )
        SellerMiniStat(
          value = soldCount.toString(),
          label = "Terjual",
          bgColor = SurfaceSoftWarm,
          valueColor = OrangeDeep,
          onClick = onOpenOrders,
          modifier = Modifier.weight(1f),
        )
      }

      // Toggle between Market Reference and My Own Items
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (!sellerOnlyFilter) OrangeDeep else SurfaceIvory,
          modifier =
            Modifier
              .weight(1f)
              .border(
                1.dp,
                if (!sellerOnlyFilter) OrangeDeep else OutlineWarm,
                RoundedCornerShape(10.dp),
              )
              .clip(RoundedCornerShape(10.dp))
              .clickable { onToggleSellerOnly(false) }
              .testTag("seller_scope_market"),
        ) {
          Box(
            modifier = Modifier.padding(vertical = 7.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Semua Pasar Kampus",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (!sellerOnlyFilter) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (sellerOnlyFilter) CrimsonPrimary else SurfaceIvory,
          modifier =
            Modifier
              .weight(1f)
              .border(
                1.dp,
                if (sellerOnlyFilter) CrimsonPrimary else OutlineWarm,
                RoundedCornerShape(10.dp),
              )
              .clip(RoundedCornerShape(10.dp))
              .clickable { onToggleSellerOnly(true) }
              .testTag("seller_scope_mine"),
        ) {
          Box(
            modifier = Modifier.padding(vertical = 7.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Barang Jualan Saya ($mySellingCount)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (sellerOnlyFilter) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SellerMiniStat(
  value: String,
  label: String,
  bgColor: Color,
  valueColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = bgColor,
    modifier = modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 7.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = valueColor,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun CampusFilterPill(
  label: String,
  selected: Boolean,
  activeColor: Color,
  onClick: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (selected) activeColor else SurfacePureWhite,
    shadowElevation = if (selected) 2.dp else 0.dp,
    modifier =
      Modifier
        .height(36.dp)
        .border(
          width = 1.dp,
          color = if (selected) activeColor else OutlineWarm,
          shape = RoundedCornerShape(10.dp),
        )
        .clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onClick)
        .testTag("campus_filter_$label"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      if (selected) {
        Icon(
          imageVector = Icons.Filled.Check,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(15.dp),
        )
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      )
    }
  }
}
