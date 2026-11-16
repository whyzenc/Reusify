package com.example.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CatalogSeedData
import com.example.data.model.Campus
import com.example.data.model.PremiumMembershipInfo
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.PremiumBadge
import com.example.ui.components.RoleModeBadge
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.OutlineSoft
import com.example.ui.theme.SurfaceBlush
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.util.formatDateShort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  profile: UserProfile?,
  membershipInfo: PremiumMembershipInfo = PremiumMembershipInfo(),
  campuses: List<Campus>,
  activeRole: UserRole,
  isAdmin: Boolean,
  sellingCount: Int,
  soldCount: Int,
  buyerOrdersCount: Int,
  unreadChatCount: Int = 0,
  onBack: () -> Unit,
  onSwitchRole: (UserRole) -> Unit,
  onOpenMyListings: () -> Unit,
  onOpenHistory: () -> Unit,
  onOpenChatHistory: () -> Unit = {},
  onOpenPremium: () -> Unit,
  onOpenAdmin: () -> Unit,
  onUpdateProfile: (String, String, String, String, String) -> Unit,
  onLogout: () -> Unit,
) {
  var showEditDialog by remember { mutableStateOf(false) }
  var showSafetyDialog by remember { mutableStateOf(false) }
  var showHelpDialog by remember { mutableStateOf(false) }

  val isSeller = activeRole == UserRole.SELLER
  val topBarColor = if (isSeller) OrangeDeep else CrimsonPrimary

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Profil Saya",
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = Color.White,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor),
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
            .navigationBarsPadding(),
      ) {
        // Blush Header matching Screenshot 1
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .background(
                Brush.verticalGradient(listOf(SurfaceBlush, MaterialTheme.colorScheme.background))
              )
              .padding(horizontal = 20.dp, vertical = 22.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
              shape = CircleShape,
              color = CrimsonPrimary,
              modifier = Modifier.size(84.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Filled.Person,
                  contentDescription = "Avatar Profil",
                  tint = Color.White,
                  modifier = Modifier.size(44.dp),
                )
              }
            }
            Surface(
              shape = CircleShape,
              color = Color.White,
              shadowElevation = 2.dp,
              modifier = Modifier.size(26.dp).offset(x = 2.dp, y = 2.dp),
            ) {
              Box(
                modifier = Modifier.padding(2.dp).background(CrimsonPrimary, CircleShape),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = "Terverifikasi",
                  tint = Color.White,
                  modifier = Modifier.size(14.dp),
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = profile?.displayName ?: "Mahasiswa Padang",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          if (!profile?.email.isNullOrBlank()) {
            Text(
              text = profile?.email ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = CrimsonPrimary,
            ) {
              Text(
                text = "Mahasiswa ${profile?.campusCode ?: "UNP"}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              )
            }
            RoleModeBadge(role = activeRole)
            if (membershipInfo.isActive) {
              PremiumBadge(label = "Premium (${membershipInfo.daysRemaining} hr)")
            }
            if (isAdmin) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = OrangeContainer,
              ) {
                Text(
                  text = "Admin",
                  style = MaterialTheme.typography.labelMedium,
                  color = OrangeDeep,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Role Switcher Card inside Profile ("Beralih Mode Pembeli / Penjual")
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfacePureWhite,
            modifier =
              Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineSoft, RoundedCornerShape(16.dp)),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Mode Tampilan Aplikasi",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (!isSeller) CrimsonPrimary else SurfaceBlush,
                  modifier =
                    Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { onSwitchRole(UserRole.BUYER) }
                      .testTag("profile_switch_buyer"),
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = "Mode Pembeli",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (!isSeller) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSeller) OrangeDeep else SurfaceBlush,
                  modifier =
                    Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { onSwitchRole(UserRole.SELLER) }
                      .testTag("profile_switch_seller"),
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = "Mode Penjual",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (isSeller) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Stats Row tailored to active role
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            if (isSeller) {
              StatCard(
                icon = Icons.Filled.Storefront,
                value = sellingCount.toString(),
                label = "Barang Dijual",
                modifier = Modifier.weight(1f),
              )
              StatCard(
                icon = Icons.Filled.CheckCircleOutline,
                value = soldCount.toString(),
                label = "Terjual",
                modifier = Modifier.weight(1f),
              )
            } else {
              StatCard(
                icon = Icons.Filled.History,
                value = buyerOrdersCount.toString(),
                label = "Total Belanja",
                modifier = Modifier.weight(1f),
              )
              StatCard(
                icon = Icons.Filled.Storefront,
                value = sellingCount.toString(),
                label = "Iklan Saya",
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // Menu List
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(0.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            if (isSeller) {
              ProfileMenuItem(
                icon = Icons.Filled.Inventory2,
                iconTint = OrangeDeep,
                title = "Kelola Barang Jualan Saya",
                onClick = onOpenMyListings,
                testTag = "menu_my_listings",
              )
              HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
              ProfileMenuItem(
                icon = Icons.Filled.History,
                iconTint = OrangeDeep,
                title = "Riwayat Penjualan & Pembelian",
                onClick = onOpenHistory,
                testTag = "menu_history",
              )
            } else {
              ProfileMenuItem(
                icon = Icons.Filled.History,
                iconTint = CrimsonPrimary,
                title = "Riwayat Pembelian & Struk QR",
                onClick = onOpenHistory,
                testTag = "menu_history",
              )
              HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
              ProfileMenuItem(
                icon = Icons.Filled.Inventory2,
                iconTint = CrimsonPrimary,
                title = "Kelola Barang Jualan Saya",
                onClick = onOpenMyListings,
                testTag = "menu_my_listings",
              )
            }
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
            ProfileMenuItem(
              icon = Icons.Filled.ChatBubbleOutline,
              iconTint = if (unreadChatCount > 0) CrimsonPrimary else OrangeDeep,
              title =
                if (unreadChatCount > 0)
                  "History Chat & Notifikasi ($unreadChatCount belum dibaca)"
                else "History Chat & Notifikasi",
              onClick = onOpenChatHistory,
              testTag = "menu_chat_history",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
            ProfileMenuItem(
              icon = Icons.Filled.PersonOutline,
              title = "Edit Profil, Kampus & Kontak WhatsApp",
              onClick = { showEditDialog = true },
              testTag = "menu_edit_profile",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
            ProfileMenuItem(
              icon = Icons.Filled.Star,
              iconTint = OrangeAccent,
              title =
                when {
                  membershipInfo.isActive && membershipInfo.expiresAt != null ->
                    "Membership 1 Bulan (s.d. ${formatDateShort(membershipInfo.expiresAt)})"
                  membershipInfo.isActive ->
                    "Membership 1 Bulan (Aktif)"
                  membershipInfo.isExpired ->
                    "Perpanjang Membership 1 Bulan (Rp 15.000)"
                  else ->
                    "Upgrade Membership 1 Bulan (Biaya Admin Rp 15.000)"
                },
              onClick = onOpenPremium,
              testTag = "menu_premium",
            )
            if (isAdmin) {
              HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
              ProfileMenuItem(
                icon = Icons.Filled.AdminPanelSettings,
                iconTint = OrangeDeep,
                title = "Panel Admin (Kelola Kampus & Premium)",
                onClick = onOpenAdmin,
                testTag = "menu_admin",
              )
            }
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
            ProfileMenuItem(
              icon = Icons.Filled.Shield,
              title = "Panduan Aman Transaksi COD Kampus",
              onClick = { showSafetyDialog = true },
              testTag = "menu_safety",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
            ProfileMenuItem(
              icon = Icons.Filled.HelpOutline,
              title = "Pusat Bantuan Reusify",
              onClick = { showHelpDialog = true },
              testTag = "menu_help",
            )
            HorizontalDivider(color = OutlineSoft.copy(alpha = 0.6f))
            ProfileMenuItem(
              icon = Icons.AutoMirrored.Filled.Logout,
              iconTint = ErrorRed,
              title = "Keluar (Logout)",
              titleColor = ErrorRed,
              onClick = onLogout,
              testTag = "menu_logout",
            )
          }
        }
      }
    }
  }

  if (showEditDialog && profile != null) {
    EditProfileDialog(
      profile = profile,
      campuses = campuses,
      onDismiss = { showEditDialog = false },
      onSave = { name, code, campusName, faculty, wa ->
        onUpdateProfile(name, code, campusName, faculty, wa)
        showEditDialog = false
      },
    )
  }

  if (showSafetyDialog) {
    AlertDialog(
      onDismissRequest = { showSafetyDialog = false },
      title = { Text("Panduan Aman COD Kampus", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("1. Selalu pilih titik temu ramai di area kampus seperti:")
          campuses.forEach { campus ->
            Text("• ${campus.code}: ${campus.meetupSpot}", style = MaterialTheme.typography.bodySmall)
          }
          Text("2. Cek kondisi fisik & kelengkapan barang sebelum menyelesaikan pembayaran.")
          Text("3. Gunakan QR Code / QRIS Reusify agar transaksi tercatat otomatis di riwayat penjual & pembeli.")
        }
      },
      confirmButton = {
        TextButton(onClick = { showSafetyDialog = false }) {
          Text("Mengerti", color = CrimsonPrimary, fontWeight = FontWeight.Bold)
        }
      },
    )
  }

  if (showHelpDialog) {
    AlertDialog(
      onDismissRequest = { showHelpDialog = false },
      title = { Text("Pusat Bantuan Reusify Padang", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("• Cara Jual Barang: Tekan '+ Jual Barang' di Beranda, unggah foto & detail barang.")
          Text("• Cara Beli & Bayar QR: Pilih barang -> metode QR Code / COD -> konfirmasi & cek Riwayat.")
          Text("• Fitur Premium & Kampus Baru: Pengajuan Premium dan penambahan kampus dikelola oleh Admin Reusify Padang.")
        }
      },
      confirmButton = {
        TextButton(onClick = { showHelpDialog = false }) {
          Text("Tutup", color = CrimsonPrimary, fontWeight = FontWeight.Bold)
        }
      },
    )
  }
}

@Composable
private fun StatCard(
  icon: ImageVector,
  value: String,
  label: String,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.border(1.dp, OutlineSoft, RoundedCornerShape(14.dp)),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = CrimsonPrimary,
        modifier = Modifier.size(24.dp),
      )
      Column {
        Text(
          text = value,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = label,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ProfileMenuItem(
  icon: ImageVector,
  title: String,
  onClick: () -> Unit,
  testTag: String,
  iconTint: Color = CrimsonPrimary,
  titleColor: Color = MaterialTheme.colorScheme.onSurface,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(22.dp),
    )
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.Medium,
      color = titleColor,
      modifier = Modifier.weight(1f),
    )
    Icon(
      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
  profile: UserProfile,
  campuses: List<Campus>,
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String) -> Unit,
) {
  var name by remember { mutableStateOf(profile.displayName) }
  var selectedCampus by remember {
    mutableStateOf(
      campuses.firstOrNull { it.code.equals(profile.campusCode, ignoreCase = true) }
        ?: campuses.firstOrNull()
        ?: CatalogSeedData.defaultCampuses.first()
    )
  }
  var faculty by remember { mutableStateOf(profile.faculty) }
  var whatsapp by remember { mutableStateOf(profile.whatsapp) }
  var campusExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Profil Mahasiswa", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nama Lengkap") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        ExposedDropdownMenuBox(
          expanded = campusExpanded,
          onExpandedChange = { campusExpanded = !campusExpanded },
        ) {
          OutlinedTextField(
            value = "${selectedCampus.code} - ${selectedCampus.name}",
            onValueChange = {},
            readOnly = true,
            label = { Text("Kampus Terdaftar") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = campusExpanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
          )
          ExposedDropdownMenu(
            expanded = campusExpanded,
            onDismissRequest = { campusExpanded = false },
          ) {
            campuses.forEach { campus ->
              DropdownMenuItem(
                text = { Text("${campus.code} - ${campus.name}") },
                onClick = {
                  selectedCampus = campus
                  campusExpanded = false
                },
              )
            }
          }
        }
        OutlinedTextField(
          value = faculty,
          onValueChange = { faculty = it },
          label = { Text("Fakultas / Jurusan") },
          placeholder = { Text("Contoh: Teknik Informatika") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = whatsapp,
          onValueChange = { whatsapp = it },
          label = { Text("Nomor WhatsApp (Opsional)") },
          placeholder = { Text("08xxxxxxxxxx") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(name, selectedCampus.code, selectedCampus.name, faculty, whatsapp)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
      ) {
        Text("Simpan")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Batal") }
    },
  )
}
