package com.example.ui.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun generateQrRawBitmap(content: String, sizePx: Int = 512): Bitmap? {
  if (content.isBlank()) return null
  return try {
    val hints = mapOf(
      EncodeHintType.MARGIN to 1,
      EncodeHintType.CHARACTER_SET to "UTF-8",
    )
    val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
      val offset = y * width
      for (x in 0 until width) {
        pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
      }
    }
    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
      setPixels(pixels, 0, width, 0, 0, width, height)
    }
  } catch (_: Exception) {
    null
  }
}

fun generateQrBitmap(content: String, sizePx: Int = 512): ImageBitmap? =
  generateQrRawBitmap(content, sizePx)?.asImageBitmap()

fun generatePaymentReceiptQrBitmap(
  qrContent: String,
  title: String,
  code: String,
  amountText: String,
  subtitle: String = "Reusify Padang • QR Pembayaran",
): Bitmap? {
  val qrSize = 620
  val qrBmp = generateQrRawBitmap(qrContent, qrSize) ?: return null
  val width = 840
  val height = 1040
  val canvasBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(canvasBmp)

  // Clean pure white background
  canvas.drawColor(Color.WHITE)

  // Top crimson header
  val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#C81E2B")
  }
  canvas.drawRect(0f, 0f, width.toFloat(), 150f, headerPaint)

  val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.WHITE
    textSize = 40f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    textAlign = Paint.Align.CENTER
  }
  canvas.drawText("Reusify Padang", width / 2f, 70f, brandPaint)

  val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#FFE4E6")
    textSize = 24f
    textAlign = Paint.Align.CENTER
  }
  canvas.drawText(subtitle.take(45), width / 2f, 114f, subPaint)

  // Subtle card border around QR
  val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = 3f
    color = Color.parseColor("#E5D9D6")
  }
  val qrLeft = (width - qrSize) / 2
  val qrTop = 190
  canvas.drawRect(
    (qrLeft - 20).toFloat(),
    (qrTop - 20).toFloat(),
    (qrLeft + qrSize + 20).toFloat(),
    (qrTop + qrSize + 20).toFloat(),
    borderPaint,
  )

  canvas.drawBitmap(qrBmp, null, Rect(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize), null)
  qrBmp.recycle()

  val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#C81E2B")
    textSize = 42f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    textAlign = Paint.Align.CENTER
  }
  canvas.drawText(amountText, width / 2f, 882f, amountPaint)

  val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#EA580C")
    textSize = 28f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    textAlign = Paint.Align.CENTER
  }
  canvas.drawText("Kode: $code", width / 2f, 932f, codePaint)

  val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#374151")
    textSize = 24f
    textAlign = Paint.Align.CENTER
  }
  val cleanTitle = if (title.length > 42) title.take(39) + "..." else title
  canvas.drawText(cleanTitle, width / 2f, 982f, titlePaint)

  return canvasBmp
}

suspend fun saveQrToGallery(
  context: Context,
  qrContent: String,
  fileNamePrefix: String,
  title: String,
  code: String,
  amountText: String,
  subtitle: String = "Reusify Padang • QR Pembayaran",
): Result<String> = withContext(Dispatchers.IO) {
  runCatching {
    val receiptBitmap =
      generatePaymentReceiptQrBitmap(
        qrContent = qrContent,
        title = title,
        code = code,
        amountText = amountText,
        subtitle = subtitle,
      ) ?: throw IllegalStateException("Gagal membuat gambar QR")

    val safeCode = code.replace(Regex("[^A-Za-z0-9_\\-]"), "_").ifBlank { "QR" }
    val safePrefix = fileNamePrefix.replace(Regex("[^A-Za-z0-9_\\-]"), "_").ifBlank { "Reusify" }
    val fileName = "${safePrefix}_${safeCode}_${System.currentTimeMillis() % 10000}.png"

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.applicationContext.contentResolver
        val values = ContentValues().apply {
          put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
          put(MediaStore.Images.Media.MIME_TYPE, "image/png")
          put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ReusifyPadang")
          put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri =
          resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Tidak dapat menyimpan ke Galeri")
        resolver.openOutputStream(uri)?.use { out ->
          if (!receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
            throw IllegalStateException("Gagal mengompres gambar QR")
          }
        } ?: throw IllegalStateException("Gagal membuka berkas Galeri")

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        "Pictures/ReusifyPadang/$fileName"
      } else {
        val dir =
          File(
            context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "ReusifyPadang",
          )
        if (!dir.exists()) dir.mkdirs()
        val targetFile = File(dir, fileName)
        FileOutputStream(targetFile).use { out ->
          if (!receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
            throw IllegalStateException("Gagal menyimpan gambar QR")
          }
        }
        MediaScannerConnection.scanFile(
          context.applicationContext,
          arrayOf(targetFile.absolutePath),
          arrayOf("image/png"),
          null,
        )
        targetFile.name
      }
    } finally {
      receiptBitmap.recycle()
    }
  }
}
