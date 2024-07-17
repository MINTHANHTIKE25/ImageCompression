package com.minthanhtike.imagecompression

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.SensorManager.getOrientation
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.FileUtils
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat


object ImageUtils {
    fun calculateImgSize(uri: Uri, context: Context): String {
        val scheme = uri.scheme
        var dataSize: Float = 0.0f
        if (scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                val fileInputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (fileInputStream != null) {
                    dataSize = fileInputStream.available().toFloat()
                }
                fileInputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (scheme == ContentResolver.SCHEME_FILE) {
            val path = uri.path
            var file: File? = null
            try {
                file = path?.let { File(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (file != null) {
                dataSize = file.length().toFloat()
            }
        }
        return DecimalFormat(".000").format(dataSize / (1024 * 1024))
    }

    // Function to get the URI and size of an image in MB
    private fun getImageUriAndSizeInMB(context: Context, uri: Uri): String? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            val sizeInBytes = inputStream?.available()?.toDouble()
            val sizeInMB = sizeInBytes?.div(1024 * 1024) // Convert bytes to MB
            return DecimalFormat(".000").format(sizeInMB).toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }


    fun encodeToBase64(imgUri: Uri, context: Context): String {

        val input = context.contentResolver.openInputStream(imgUri)
        val imgSize = getImageUriAndSizeInMB(context, uri = imgUri)?.toDouble() ?: 0.0
        // Encode image to base64 string
        return try {
            if (imgSize < 1) {
                val inputStream = input?.use { input ->
                    val bytes = input.readBytes()
                    val orientation = getOrientation(context, imgUri)
                    val rotatedBitmap = rotateBitmap(
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size),
                        orientation
                    )
                    val outputStream = ByteArrayOutputStream()
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream
                }

                val compressSize = inputStream?.size()?.div((1024 * 1024).toDouble())
                Log.d("CompressSize", "compressImg: $compressSize")
                Base64.encodeToString(inputStream?.toByteArray(), Base64.DEFAULT)
            } else {
                val inputStream = input?.use { input ->
                    val bytes = input.readBytes()
                    val orientation = getOrientation(context, imgUri)
                    val rotatedBitmap = rotateBitmap(
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size),
                        orientation
                    )
                    val outputStream = ByteArrayOutputStream()
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

                    outputStream
                }

                val compressSize = inputStream?.size()?.div((1024 * 1024).toDouble())
                Log.d("CompressSize", "compressImg: $compressSize")
                Base64.encodeToString(inputStream?.toByteArray(), Base64.DEFAULT)
            }

        } catch (e: IOException) {
            e.localizedMessage ?: "Don't know error!"
        } finally {
            input?.close()
        }
    }

//    /**
//     * This encoding is for the default image to upload to the server
//     * @param imgBitmap -> use the bitmap because the photo is in the drawable.Subsequently, we
//     * need the image in bitmap
//     */
//    fun encodeToBase64(context: Context, drawableId: Int): String? {
//        val drawable = ContextCompat.getDrawable(context, drawableId)
//        if (drawable != null) {
//            val bitmap = getBitmapFromDrawable(drawable)
//            val outputStream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            val byteArray = outputStream.toByteArray()
//            return Base64.encodeToString(byteArray, Base64.DEFAULT)
//        }
//        return null
//    }
//
//    /**
//     *Change the drawable to bitmap
//     */
//    // Function to convert Drawable to Bitmap
//    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
//        val bitmap = Bitmap.createBitmap(
//            drawable.intrinsicWidth,
//            drawable.intrinsicHeight,
//            Bitmap.Config.ARGB_8888
//        )
//        val canvas = Canvas(bitmap)
//        drawable.setBounds(0, 0, canvas.width, canvas.height)
//        drawable.draw(canvas)
//        return bitmap
//    }

    fun base64toBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    // Function to get the orientation of the image
    private fun getOrientation(context: Context, uri: Uri): Float {
        val uriPath = uri.path ?: ""
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            val exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExifInterface(stream)
            } else {
                ExifInterface(uriPath)
            }
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }
        return 0f
    }

    // Function to rotate the bitmap
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        // Rotate the bitmap
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val targetHeight = bitmap.height.div(1.5)
        val targetWidth = bitmap.width.div(1.5)
        // Calculate the scaling factors for resizing
        val scaleX = targetWidth.toFloat() / rotatedBitmap.width
        val scaleY = targetHeight.toFloat() / rotatedBitmap.height
        val scale = scaleX.coerceAtMost(scaleY) // Maintain aspect ratio by choosing the smaller scaling factor

        // Resize the bitmap while maintaining aspect ratio
        val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap,
            (rotatedBitmap.width * scale).toInt(),
            (rotatedBitmap.height * scale).toInt(),
            true)

        return scaledBitmap
    }

}