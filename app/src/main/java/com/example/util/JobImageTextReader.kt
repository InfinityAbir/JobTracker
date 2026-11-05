package com.example.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Reads the text out of a job-posting screenshot or photo, entirely on the device.
 * Nothing about the image is uploaded; only the extracted text may later be sent to the AI parser.
 */
object JobImageTextReader {

    /** Shorter than the AI parser's input cap so a long posting is not rejected downstream. */
    private const val MAX_CHARS = 8000

    sealed interface Result {
        data class Success(val text: String) : Result
        data object NoText : Result
        data object Failed : Result
    }

    suspend fun read(context: Context, uri: Uri): Result {
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            return Result.Failed
        }

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            suspendCancellableCoroutine { cont ->
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val cleaned = tidy(visionText.text)
                        cont.resume(if (cleaned.isBlank()) Result.NoText else Result.Success(cleaned))
                    }
                    .addOnFailureListener { cont.resume(Result.Failed) }
            }
        } finally {
            recognizer.close()
        }
    }

    /** Trims trailing spaces, collapses runs of blank lines and caps the length. Visible for tests. */
    internal fun tidy(raw: String): String =
        raw.lines()
            .joinToString("\n") { it.trimEnd() }
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
            .take(MAX_CHARS)
}
