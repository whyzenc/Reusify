package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.data.RolePreferenceStore
import com.example.data.model.UserRole
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedContainer
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.OutlineWarm
import com.example.ui.theme.SurfaceIvory
import com.example.ui.theme.SurfacePureWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var selectedRole by remember {
    mutableStateOf(RolePreferenceStore.getPendingRole(context))
  }
  var isAdminSelected by remember {
    mutableStateOf(RolePreferenceStore.getPendingAdminIntent(context))
  }
  var showRegisterFields by remember { mutableStateOf(false) }
  var username by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = scope,
    )
  }

  val ctaBrush =
    when {
      isAdminSelected -> Brush.horizontalGradient(listOf(CrimsonDark, CrimsonPrimary))
      selectedRole == UserRole.SELLER -> Brush.horizontalGradient(listOf(OrangeDeep, CrimsonPrimary))
      else -> Brush.horizontalGradient(listOf(CrimsonPrimary, OrangeAccent))
    }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(CrimsonDark, CrimsonPrimary, OrangeAccent.copy(alpha = 0.22f), SurfaceIvory),
            startY = 0f,
            endY = 920f,
          )
        )
        .statusBarsPadding()
        .navigationBarsPadding(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 520.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Brand Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(38.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
              )
            }
          }
          Text(
            text = "Reusify Padang",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Marketplace Barang Bekas Mahasiswa Se-Padang",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.94f),
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Crisp Pure White Showcase Card
        Card(
          shape = RoundedCornerShape(22.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
          colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Image(
              painter = painterResource(id = R.drawable.reusify_campus_hero_1794819655070),
              contentDescription = "Ilustrasi Mahasiswa Padang bertukar barang bekas kampus",
              contentScale = ContentScale.Crop,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .height(155.dp)
                  .clip(RoundedCornerShape(16.dp)),
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              listOf("UNP", "PNP", "UNAND", "UBH").forEach { code ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = CrimsonContainer,
                ) {
                  Text(
                    text = code,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  )
                }
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = OrangeContainer,
              ) {
                Text(
                  text = "+ Kampus Lainnya",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = OrangeDeep,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role Selection + New Account Registration Data Card
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
          modifier =
            Modifier
              .fillMaxWidth()
              .border(1.dp, OutlineWarm, RoundedCornerShape(20.dp)),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Pilih Peran Masuk",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = OrangeContainer,
              ) {
                Text(
                  text = "Pembeli / Penjual",
                  style = MaterialTheme.typography.labelSmall,
                  color = OrangeDeep,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              RoleSelectionCard(
                role = UserRole.BUYER,
                selected = !isAdminSelected && selectedRole == UserRole.BUYER,
                accentColor = CrimsonPrimary,
                accentContainer = CrimsonContainer,
                icon = Icons.Filled.ShoppingBag,
                onClick = {
                  isAdminSelected = false
                  selectedRole = UserRole.BUYER
                  RolePreferenceStore.savePendingAdminIntent(context, false)
                  RolePreferenceStore.savePendingRole(context, UserRole.BUYER)
                },
                modifier = Modifier.weight(1f).testTag("role_card_buyer"),
              )
              RoleSelectionCard(
                role = UserRole.SELLER,
                selected = !isAdminSelected && selectedRole == UserRole.SELLER,
                accentColor = OrangeDeep,
                accentContainer = OrangeContainer,
                icon = Icons.Filled.Storefront,
                onClick = {
                  isAdminSelected = false
                  selectedRole = UserRole.SELLER
                  RolePreferenceStore.savePendingAdminIntent(context, false)
                  RolePreferenceStore.savePendingRole(context, UserRole.SELLER)
                },
                modifier = Modifier.weight(1f).testTag("role_card_seller"),
              )
            }

            // Dedicated Admin Verifier Login Card
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isAdminSelected) CrimsonContainer.copy(alpha = 0.65f) else SurfaceIvory,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .border(
                    width = if (isAdminSelected) 2.dp else 1.dp,
                    color = if (isAdminSelected) CrimsonPrimary else OutlineWarm,
                    shape = RoundedCornerShape(14.dp),
                  )
                  .clip(RoundedCornerShape(14.dp))
                  .clickable {
                    isAdminSelected = true
                    RolePreferenceStore.savePendingAdminIntent(context, true)
                  }
                  .testTag("role_card_admin"),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isAdminSelected) CrimsonPrimary else CrimsonContainer,
                  modifier = Modifier.size(36.dp),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Filled.AdminPanelSettings,
                      contentDescription = null,
                      tint = if (isAdminSelected) Color.White else CrimsonPrimary,
                      modifier = Modifier.size(20.dp),
                    )
                  }
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Masuk sebagai Admin (Verifikasi)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAdminSelected) CrimsonPrimary else MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "Verifikasi Membership Rp 15k, Transaksi & Kampus via akun Google Admin",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                if (isAdminSelected) {
                  Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Admin dipilih",
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }

            // Expandable Student Registration Fields (Username & Nomor HP) for first-time Google Sign-In
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = SurfaceIvory,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .border(1.dp, OutlineWarm, RoundedCornerShape(12.dp))
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { showRegisterFields = !showRegisterFields }
                  .testTag("toggle_register_fields"),
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
                  Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = "Belum punya profil? Isi Username & No. HP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
                Icon(
                  imageVector =
                    if (showRegisterFields) Icons.Filled.KeyboardArrowUp
                    else Icons.Filled.KeyboardArrowDown,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            AnimatedVisibility(visible = showRegisterFields) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = username,
                  onValueChange = { username = it },
                  label = { Text("Username / Nama Tampilan") },
                  placeholder = { Text("Contoh: Amelia Putri UNP") },
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth().testTag("register_username_input"),
                )
                OutlinedTextField(
                  value = phoneNumber,
                  onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '+' } && input.length <= 16) {
                      phoneNumber = input
                    }
                  },
                  label = { Text("Nomor HP / WhatsApp") },
                  placeholder = { Text("08xxxxxxxxxx") },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Filled.Phone,
                      contentDescription = null,
                      tint = OrangeDeep,
                    )
                  },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth().testTag("register_phone_input"),
                )
                Text(
                  text = "Data username & nomor HP otomatis tersimpan ke akun Anda saat verifikasi Google di bawah.",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        if (errorMessage != null) {
          Surface(
            color = ErrorRedContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
          ) {
            Text(
              text = errorMessage!!,
              style = MaterialTheme.typography.bodySmall,
              color = ErrorRed,
              modifier = Modifier.padding(12.dp),
            )
          }
        }

        // Gradient Red-Orange Sign-In CTA
        Button(
          onClick = {
            RolePreferenceStore.savePendingRole(context, selectedRole)
            RolePreferenceStore.savePendingAdminIntent(context, isAdminSelected)
            RolePreferenceStore.savePendingRegistration(context, username, phoneNumber)
            errorMessage = null
            isLoading = true
            onGoogleSignInClicked(
              context = context,
              credentialManager = credentialManager,
              onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
              },
              onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
              },
              scope = scope,
              onAuthCancelled = {
                isLoading = false
              },
            )
          },
          enabled = !isLoading,
          modifier =
            Modifier
              .fillMaxWidth()
              .height(54.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(ctaBrush)
              .testTag("google_sign_in_button"),
          shape = RoundedCornerShape(14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              disabledContainerColor = CrimsonPrimary.copy(alpha = 0.55f),
            ),
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = Color.White,
              strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Menghubungkan Akun Google...", color = Color.White)
          } else {
            Icon(
              imageVector = if (isAdminSelected) Icons.Filled.AdminPanelSettings else Icons.Filled.VerifiedUser,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text =
                if (isAdminSelected) "Masuk sebagai Admin • Google"
                else "Masuk sebagai ${selectedRole.label} • Google",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Masuk aman terverifikasi Google untuk mahasiswa UNP, PNP, UNAND, UBH & kampus Padang",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
private fun RoleSelectionCard(
  role: UserRole,
  selected: Boolean,
  accentColor: Color,
  accentContainer: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (selected) accentContainer.copy(alpha = 0.55f) else SurfacePureWhite,
    modifier =
      modifier
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = if (selected) accentColor else OutlineWarm,
          shape = RoundedCornerShape(16.dp),
        )
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (selected) accentColor else accentContainer,
          modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = if (selected) Color.White else accentColor,
              modifier = Modifier.size(20.dp),
            )
          }
        }
        if (selected) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "${role.label} dipilih",
            tint = accentColor,
            modifier = Modifier.size(20.dp),
          )
        }
      }
      Text(
        text = role.label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text =
          if (role == UserRole.BUYER) "Belanja & bayar QR / COD"
          else "Jual barang & kelola order",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
