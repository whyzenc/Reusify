package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.R
import com.google.firebase.Timestamp
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatRupiah(amount: Long): String {
  val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
  return "Rp ${formatter.format(amount)}"
}

fun formatTimestamp(timestamp: Timestamp?): String {
  val date = timestamp?.toDate() ?: Date()
  val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
  return fmt.format(date)
}

fun formatDateShort(timestamp: Timestamp?): String {
  val date = timestamp?.toDate() ?: Date()
  val fmt = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
  return fmt.format(date)
}

fun calculateOneMonthExpiry(startTimestamp: Timestamp?): Timestamp? {
  val start = startTimestamp ?: return null
  val cal = Calendar.getInstance()
  cal.time = start.toDate()
  cal.add(Calendar.MONTH, 1)
  return Timestamp(cal.time)
}

const val MAX_ITEM_IMAGES = 5
private const val IMAGE_LIST_DELIMITER = "\n"
private const val MAX_FIRESTORE_IMAGE_PAYLOAD_CHARS = 135_000

fun decodeImageUrls(raw: String): List<String> {
  if (raw.isBlank()) return emptyList()
  return raw
    .split(IMAGE_LIST_DELIMITER, "|")
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinct()
    .take(MAX_ITEM_IMAGES)
}

fun encodeImageUrls(urls: List<String>): String =
  urls
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinct()
    .take(MAX_ITEM_IMAGES)
    .joinToString(IMAGE_LIST_DELIMITER)

fun primaryImageUrl(raw: String): String = decodeImageUrls(raw).firstOrNull().orEmpty()

fun decodeImageModel(imageUrl: String): Any? {
  val first = primaryImageUrl(imageUrl)
  if (first.isBlank()) return null
  if (first == "local://setrika_philips") {
    return R.drawable.img_setrika_philips_1794828826774
  }
  if (first.startsWith("data:image")) {
    val base64Part = first.substringAfter("base64,", "")
    if (base64Part.isBlank()) return null
    return try {
      Base64.decode(base64Part, Base64.DEFAULT)
    } catch (_: Exception) {
      null
    }
  }
  return first
}

fun compressUriToDataUri(
  context: Context,
  uri: Uri,
  maxDimPx: Int = 420,
  quality: Int = 62,
): String? {
  return try {
    val input = context.contentResolver.openInputStream(uri) ?: return null
    val original = input.use { BitmapFactory.decodeStream(it) } ?: return null
    val width = original.width
    val height = original.height
    val scale =
      if (width > maxDimPx || height > maxDimPx) {
        minOf(maxDimPx.toFloat() / width, maxDimPx.toFloat() / height)
      } else {
        1f
      }
    val scaled =
      if (scale < 1f) {
        Bitmap.createScaledBitmap(
          original,
          (width * scale).toInt().coerceAtLeast(1),
          (height * scale).toInt().coerceAtLeast(1),
          true,
        )
      } else {
        original
      }

    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
    if (scaled !== original) scaled.recycle()
    original.recycle()
    val encoded = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    "data:image/jpeg;base64,$encoded"
  } catch (_: Exception) {
    null
  }
}

fun compressMultipleUrisToDataUris(
  context: Context,
  uris: List<Uri>,
  existingImages: List<String> = emptyList(),
): List<String> {
  val remainingSlots = (MAX_ITEM_IMAGES - existingImages.size).coerceAtLeast(0)
  if (remainingSlots == 0 || uris.isEmpty()) return existingImages

  val selectedUris = uris.take(remainingSlots)
  val totalCount = (existingImages.size + selectedUris.size).coerceAtLeast(1)
  val existingDataChars = existingImages.sumOf { it.length }
  val remainingBudget = (MAX_FIRESTORE_IMAGE_PAYLOAD_CHARS - existingDataChars).coerceAtLeast(22_000)
  val perNewPhotoBudget = (remainingBudget / selectedUris.size).coerceIn(14_000, 55_000)

  val newEncoded =
    selectedUris.mapNotNull { uri ->
      var candidate = compressUriToDataUri(context, uri, maxDimPx = 420, quality = 62)
      if (candidate != null && candidate.length > perNewPhotoBudget) {
        candidate = compressUriToDataUri(context, uri, maxDimPx = 320, quality = 50)
      }
      if (candidate != null && candidate.length > perNewPhotoBudget) {
        candidate = compressUriToDataUri(context, uri, maxDimPx = 240, quality = 42)
      }
      candidate
    }

  val combined = (existingImages + newEncoded).distinct().take(MAX_ITEM_IMAGES)
  // Re-normalize if still slightly above budget due to earlier large single photo
  val totalLength = encodeImageUrls(combined).length
  if (totalLength <= MAX_FIRESTORE_IMAGE_PAYLOAD_CHARS) return combined

  val perItemTarget = (MAX_FIRESTORE_IMAGE_PAYLOAD_CHARS / totalCount.coerceAtLeast(1)) - 10
  return combined.map { entry ->
    if (entry.startsWith("data:image") && entry.length > perItemTarget) {
      recompressDataUriToBudget(entry, perItemTarget)
    } else {
      entry
    }
  }
}

private fun recompressDataUriToBudget(dataUri: String, maxChars: Int): String {
  return try {
    val rawBytes = Base64.decode(dataUri.substringAfter("base64,", ""), Base64.DEFAULT)
    val bmp = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size) ?: return dataUri
    val attempts = listOf(300 to 48, 240 to 42, 180 to 38)
    var best = dataUri
    for ((dim, q) in attempts) {
      val scale = minOf(dim.toFloat() / bmp.width, dim.toFloat() / bmp.height, 1f)
      val resized =
        if (scale < 1f) {
          Bitmap.createScaledBitmap(
            bmp,
            (bmp.width * scale).toInt().coerceAtLeast(1),
            (bmp.height * scale).toInt().coerceAtLeast(1),
            true,
          )
        } else {
          bmp
        }
      val out = ByteArrayOutputStream()
      resized.compress(Bitmap.CompressFormat.JPEG, q, out)
      if (resized !== bmp) resized.recycle()
      val encoded = "data:image/jpeg;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
      best = encoded
      if (encoded.length <= maxChars) break
    }
    bmp.recycle()
    best
  } catch (_: Exception) {
    dataUri
  }
}
