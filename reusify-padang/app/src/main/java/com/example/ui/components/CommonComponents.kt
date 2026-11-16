package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.MarketItem
import com.example.data.model.TransactionStatus
import com.example.data.model.UserRole
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedContainer
import com.example.ui.theme.OnOrangeContainer
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.OutlineWarm
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.SurfaceIvory
import com.example.ui.theme.SurfaceMutedWarm
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.util.decodeImageModel
import com.example.ui.util.decodeImageUrls
import com.example.ui.util.formatRupiah

@Composable
fun RoleModeBadge(
  role: UserRole,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  val isSeller = role == UserRole.SELLER
  val bgColor = if (isSeller) OrangeContainer else CrimsonContainer
  val fgColor = if (isSeller) OrangeDeep else CrimsonPrimary
  val icon = if (isSeller) Icons.Filled.Storefront else Icons.Filled.ShoppingBag

  Surface(
    modifier =
      modifier
        .clip(RoundedCornerShape(20.dp))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    shape = RoundedCornerShape(20.dp),
    color = bgColor,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = fgColor,
        modifier = Modifier.size(14.dp),
      )
      Text(
        text = "Mode ${role.label}",
        style = MaterialTheme.typography.labelSmall,
        color = fgColor,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
fun CampusBadge(
  campusCode: String,
  modifier: Modifier = Modifier,
  showIcon: Boolean = false,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(6.dp),
    color = CrimsonContainer,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      if (showIcon) {
        Icon(
          imageVector = Icons.Filled.School,
          contentDescription = null,
          tint = CrimsonPrimary,
          modifier = Modifier.size(13.dp),
        )
      }
      Text(
        text = campusCode,
        style = MaterialTheme.typography.labelSmall,
        color = CrimsonPrimary,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
fun PremiumBadge(modifier: Modifier = Modifier, label: String = "Premium") {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = OrangeContainer,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.Star,
        contentDescription = null,
        tint = OrangeAccent,
        modifier = Modifier.size(12.dp),
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = OnOrangeContainer,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
fun TransactionStatusBadge(status: String, modifier: Modifier = Modifier) {
  val (label, bgColor, fgColor) =
    when (status) {
      TransactionStatus.PENDING_PAYMENT ->
        Triple("Menunggu Bayar", OrangeContainer, OrangeDeep)
      TransactionStatus.PAID ->
        Triple("Dibayar", CrimsonContainer, CrimsonPrimary)
      TransactionStatus.COMPLETED ->
        Triple("Selesai", SuccessGreenContainer, SuccessGreen)
      TransactionStatus.CANCELLED ->
        Triple("Dibatalkan", ErrorRedContainer, ErrorRed)
      else -> Triple(status, SurfaceMutedWarm, MaterialTheme.colorScheme.onSurfaceVariant)
    }
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = bgColor,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = fgColor,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
  }
}

@Composable
fun MarketItemImage(
  imageUrl: String,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  val model = decodeImageModel(imageUrl)
  if (model == null) {
    Box(
      modifier = modifier.background(SurfaceMutedWarm),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.Image,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(36.dp),
      )
    }
  } else {
    SubcomposeAsyncImage(
      model = model,
      contentDescription = contentDescription,
      contentScale = ContentScale.Crop,
      modifier = modifier,
      loading = {
        Box(
          modifier = Modifier.fillMaxSize().background(SurfaceMutedWarm),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(28.dp),
          )
        }
      },
      error = {
        Box(
          modifier = Modifier.fillMaxSize().background(SurfaceMutedWarm),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.size(32.dp),
          )
        }
      },
    )
  }
}

@Composable
fun SwipeableItemImageCarousel(
  imageUrl: String,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  val images = decodeImageUrls(imageUrl)
  if (images.size <= 1) {
    MarketItemImage(
      imageUrl = images.firstOrNull().orEmpty(),
      contentDescription = contentDescription,
      modifier = modifier,
    )
  } else {
    val pagerState = rememberPagerState(pageCount = { images.size })
    Box(modifier = modifier) {
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize().testTag("item_image_pager"),
      ) { page ->
        MarketItemImage(
          imageUrl = images[page],
          contentDescription = "$contentDescription (${page + 1}/${images.size})",
          modifier = Modifier.fillMaxSize(),
        )
      }

      // Slide counter pill at bottom-end
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.62f),
        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Collections,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(13.dp),
          )
          Text(
            text = "${pagerState.currentPage + 1}/${images.size}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // Page indicator dots at bottom-center
      Row(
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 14.dp)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        repeat(images.size) { index ->
          val active = pagerState.currentPage == index
          Box(
            modifier =
              Modifier
                .size(if (active) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(if (active) Color.White else Color.White.copy(alpha = 0.45f))
          )
        }
      }
    }
  }
}

@Composable
fun FeaturedTopProductCard(
  item: MarketItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isOwnedByViewer: Boolean = false,
  accentColor: Color = OrangeDeep,
) {
  val imageCount = decodeImageUrls(item.imageUrl).size
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .border(
          width = 1.5.dp,
          color = OrangeAccent,
          shape = RoundedCornerShape(18.dp),
        )
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .testTag("featured_product_card_${item.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top gold-orange highlight strip
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(OrangeContainer.copy(alpha = 0.72f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = OrangeDeep,
            modifier = Modifier.size(14.dp),
          )
          Text(
            text = "Sorotan Premium",
            style = MaterialTheme.typography.labelSmall,
            color = OnOrangeContainer,
            fontWeight = FontWeight.Bold,
          )
        }
        if (isOwnedByViewer) {
          Surface(shape = RoundedCornerShape(6.dp), color = OrangeDeep) {
            Text(
              text = "Iklan Saya",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            )
          }
        } else {
          Text(
            text = item.category,
            style = MaterialTheme.typography.labelSmall,
            color = OrangeDeep,
            fontWeight = FontWeight.Medium,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().background(SurfaceIvory.copy(alpha = 0.35f)).padding(11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .size(104.dp)
              .clip(RoundedCornerShape(14.dp)),
        ) {
          MarketItemImage(
            imageUrl = item.imageUrl,
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
          )
          if (imageCount > 1) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.Black.copy(alpha = 0.62f),
              modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Collections,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(11.dp),
                )
                Text(
                  text = "$imageCount",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            CampusBadge(campusCode = item.campusCode)
            if (item.sellerPremium) {
              Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "Penjual Terverifikasi Premium",
                tint = OrangeDeep,
                modifier = Modifier.size(15.dp),
              )
            }
          }
          Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = item.sellerName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = formatRupiah(item.price),
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
fun ProductGridCard(
  item: MarketItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isOwnedByViewer: Boolean = false,
  accentColor: Color = CrimsonPrimary,
) {
  val imageCount = decodeImageUrls(item.imageUrl).size
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .border(
          width = if (isOwnedByViewer) 1.5.dp else 1.dp,
          color = if (isOwnedByViewer) OrangeAccent.copy(alpha = 0.65f) else OutlineWarm,
          shape = RoundedCornerShape(18.dp),
        )
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .testTag("product_card_${item.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column {
      Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.05f)) {
        MarketItemImage(
          imageUrl = item.imageUrl,
          contentDescription = item.title,
          modifier = Modifier.fillMaxSize(),
        )
        if (isOwnedByViewer) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = OrangeDeep,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
          ) {
            Text(
              text = "Iklan Saya",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
        } else if (item.isFeatured || item.sellerPremium) {
          PremiumBadge(
            label = if (item.isFeatured) "Sorotan" else "Premium",
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
          )
        }
        if (imageCount > 1) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Collections,
                contentDescription = "$imageCount foto",
                tint = Color.White,
                modifier = Modifier.size(11.dp),
              )
              Text(
                text = "$imageCount",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
      Column(
        modifier = Modifier.padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CampusBadge(campusCode = item.campusCode)
          if (item.sellerPremium) {
            Icon(
              imageVector = Icons.Filled.Verified,
              contentDescription = "Penjual Terverifikasi",
              tint = OrangeDeep,
              modifier = Modifier.size(14.dp),
            )
          }
        }
        Text(
          text = item.title,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          minLines = 2,
        )
        Text(
          text = formatRupiah(item.price),
          style = MaterialTheme.typography.titleMedium,
          color = accentColor,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
fun EmptyStateView(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = OrangeContainer,
      modifier = Modifier.size(74.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = CrimsonPrimary,
          modifier = Modifier.size(36.dp),
        )
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
  }
}
