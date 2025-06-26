package com.example.storyapp.addStory

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.storyapp.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.exifinterface.media.ExifInterface


class addStoryHelper {
    companion object {

        private const val MAX_IMAGE_SIZE = 1_000_000  // Maksimal ukuran gambar dalam byte
        private const val TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss"
        private val uniqueTimestamp = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.US).format(Date())

        @SuppressLint("ObsoleteSdkInt")
        fun generateImageUri(context: Context): Uri {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$uniqueTimestamp.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CustomFolder/")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                createUriForLegacyVersions(context)
            }

            return uri ?: createUriForLegacyVersions(context)
        }

        private fun createUriForLegacyVersions(context: Context): Uri {
            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File(picturesDir, "/CustomFolder/$uniqueTimestamp.jpg")
            imageFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
            return FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                imageFile
            )
        }

        fun File.compressImageToFile(): File {
            val bitmap = BitmapFactory.decodeFile(this.path).rotateImageIfNeeded(this)
            var quality = 100
            var fileSize: Int

            do {
                val byteStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
                fileSize = byteStream.toByteArray().size
                quality -= 5
            } while (fileSize > MAX_IMAGE_SIZE)

            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, FileOutputStream(this))
            return this
        }

        private fun Bitmap.rotateImageIfNeeded(file: File): Bitmap {
            val exif = ExifInterface(file)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> this.rotate(90F)
                ExifInterface.ORIENTATION_ROTATE_180 -> this.rotate(180F)
                ExifInterface.ORIENTATION_ROTATE_270 -> this.rotate(270F)
                else -> this
            }
        }

        private fun Bitmap.rotate(angle: Float): Bitmap {
            val matrix = Matrix().apply { postRotate(angle) }
            return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
        }

        fun convertUriToFile(context: Context, uri: Uri): File {
            val tempFile = File.createTempFile(uniqueTimestamp, ".jpg", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        }
    }
}
