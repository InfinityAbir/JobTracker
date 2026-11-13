package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Reads the text out of a job-posting screenshot or photo, entirely on the device.
 * Nothing about the image is uploaded; only the extracted text may later be sent to the AI parser.
 */
object JobImageTextReader {

    private const val TAG = "JobImageTextReader"

    /** Shorter than the AI parser's input cap so a long posting is not rejected downstream. */
    private const val MAX_CHARS = 8000

    /** Longest edge fed to the recognizer. Big enough for small print, small enough to avoid OOM. */
    private const val MAX_EDGE_PX = 3000

    sealed interface Result {
        data class Success(val text: String) : Result
        data object NoText : Result
        data object Failed : Result
    }

    suspend fun read(context: Context, uri: Uri): Result {
        val bitmap = withContext(Dispatchers.IO) { decode(context, uri) } ?: return Result.Failed

        // getClient can throw if the ML Kit module failed to initialise; never let that crash the sheet.
        val recognizer = try {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (t: Throwable) {
            Log.e(TAG, "OCR init failed", t)
            bitmap.recycle()
            return Result.Failed
        }

        return try {
            suspendCancellableCoroutine { cont ->
                try {
                    recognizer.process(InputImage.fromBitmap(bitmap, 0))
                        .addOnSuccessListener { visionText ->
                            val cleaned = tidy(visionText.text)
                            cont.resume(if (cleaned.isBlank()) Result.NoText else Result.Success(cleaned))
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "OCR failed", e)
                            cont.resume(Result.Failed)
                        }
                } catch (t: Throwable) {
                    Log.e(TAG, "OCR could not start", t)
                    cont.resume(Result.Failed)
                }
            }
        } finally {
            recognizer.close()
            bitmap.recycle()
        }
    }

    /**
     * Decodes [uri] to a downscaled, upright ARGB bitmap, or null when it cannot be read.
     * Done by hand (instead of InputImage.fromFilePath) so huge photos cannot OOM and
     * picker/content URIs of any provider work.
     */
    private fun decode(context: Context, uri: Uri): Bitmap? = try {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            Log.e(TAG, "Unsupported or unreadable image: $uri (${bounds.outMimeType})")
            null
        } else {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            val rotation = resolver.openInputStream(uri)?.use { exifRotation(it) } ?: 0
            decoded?.let { upright(it, rotation) }
        }
    } catch (t: Throwable) {
        Log.e(TAG, "Could not decode image: $uri", t)
        null
    }

    private fun exifRotation(stream: java.io.InputStream): Int = try {
        when (
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (_: Throwable) {
        0 // PNG screenshots and the like have no EXIF; that is fine.
    }

    private fun upright(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val rotated = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height,
            Matrix().apply { postRotate(degrees.toFloat()) }, true
        )
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }

    /** Power-of-two sample size that keeps the longest edge near [MAX_EDGE_PX]. Visible for tests. */
    internal fun sampleSizeFor(width: Int, height: Int): Int {
        var sample = 1
        var longest = maxOf(width, height)
        while (longest / 2 >= MAX_EDGE_PX) {
            sample *= 2
            longest /= 2
        }
        return sample
    }

    /** Trims trailing spaces, collapses runs of blank lines and caps the length. Visible for tests. */
    internal fun tidy(raw: String): String =
        raw.lines()
            .joinToString("\n") { it.trimEnd() }
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
            .take(MAX_CHARS)
}
