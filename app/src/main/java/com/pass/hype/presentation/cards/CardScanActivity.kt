package com.pass.hype.presentation.cards

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Launches Google ML Kit's Document Scanner to capture a clean, deskewed photo
 * of a card (front + optional back), then runs ML Kit Text Recognition (OCR) on
 * the front image to extract card number, expiry date and cardholder name.
 *
 * Results are returned via setResult(RESULT_OK) with the EXTRA_* constants below.
 */
class CardScanActivity : ComponentActivity() {

    private lateinit var scannerLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val pages = GmsDocumentScanningResult
                    .fromActivityResultIntent(result.data)
                    ?.pages
                if (pages.isNullOrEmpty()) {
                    cancelAndFinish()
                } else {
                    lifecycleScope.launch { processPages(pages) }
                }
            } else {
                cancelAndFinish()
            }
        }

        if (savedInstanceState == null) launchScanner()
    }

    // ── Scanner ──────────────────────────────────────────────────────────────

    private fun launchScanner() {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(2)                                          // front + back
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)  // perspective + enhancement
            .build()

        GmsDocumentScanning.getClient(options)
            .getStartScanIntent(this)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { cancelAndFinish() }
    }

    // ── Processing ───────────────────────────────────────────────────────────

    private suspend fun processPages(pages: List<GmsDocumentScanningResult.Page>) {
        try {
            val frontUri = pages[0].imageUri

            // Save scanned images to private storage in parallel-ish
            val frontPath = withContext(Dispatchers.IO) { copyToStorage(frontUri, "front") }
            val backPath  = if (pages.size > 1) {
                withContext(Dispatchers.IO) { copyToStorage(pages[1].imageUri, "back") }
            } else null

            // OCR on the front image
            val cardData = withContext(Dispatchers.IO) { extractCardData(frontUri) }

            setResult(RESULT_OK, Intent().apply {
                putExtra(EXTRA_PAN,             cardData.pan)
                putExtra(EXTRA_EXPIRY_MONTH,    cardData.expiryMonth)
                putExtra(EXTRA_EXPIRY_YEAR,     cardData.expiryYear)
                putExtra(EXTRA_CARDHOLDER_NAME, cardData.holderName)
                putExtra(EXTRA_FRONT_IMAGE_PATH, frontPath)
                backPath?.let { putExtra(EXTRA_BACK_IMAGE_PATH, it) }
            })
        } catch (_: Exception) {
            setResult(RESULT_CANCELED)
        } finally {
            finish()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun copyToStorage(uri: Uri, side: String): String? = try {
        val dir  = File(filesDir, "card_photos").also { it.mkdirs() }
        val dest = File(dir, "${side}_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { src ->
            dest.outputStream().use { dst -> src.copyTo(dst) }
        }
        dest.absolutePath
    } catch (_: Exception) { null }

    private suspend fun extractCardData(imageUri: Uri): CardData {
        val inputImage = InputImage.fromFilePath(this, imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val visionText = suspendCancellableCoroutine { cont ->
            recognizer.process(inputImage)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

        val fullText = visionText.text
        val lines = visionText.textBlocks
            .flatMap { it.lines }
            .map { it.text.trim() }
            .filter { it.isNotBlank() }

        // 16-digit card number — groups of 4, optionally separated by space or dash
        val cardNumberRegex = Regex("""(\d{4})[\s\-]?(\d{4})[\s\-]?(\d{4})[\s\-]?(\d{4})""")
        val pan = cardNumberRegex.find(fullText)
            ?.groupValues
            ?.drop(1)               // skip full match
            ?.joinToString("")
            ?.takeIf { it.length == 16 }

        // Expiry MM/YY or MM/YYYY
        val expiryRegex = Regex("""(0?[1-9]|1[0-2])[/\-](\d{2}|\d{4})""")
        val expiryMatch = expiryRegex.find(fullText)
        val expiryMonth = expiryMatch?.groupValues?.getOrNull(1)?.padStart(2, '0')
        val expiryYear  = expiryMatch?.groupValues?.getOrNull(2)?.takeLast(2)

        // Cardholder name: longest all-caps line, 2–4 words, no digits, no card jargon
        val jargon = setOf(
            "VALID", "THRU", "FROM", "DEBIT", "CREDIT",
            "VISA", "MASTERCARD", "MAESTRO", "RUPAY", "AMEX",
            "BANK", "CARD", "MEMBER", "SINCE", "EXPIRES"
        )
        val holderName = lines
            .filter { line ->
                line.matches(Regex("[A-Z][A-Z ]{4,38}")) &&
                !line.contains(Regex("""\d""")) &&
                line.trim().split(Regex("""\s+""")).let { words ->
                    words.size in 2..4 && words.none { it in jargon }
                }
            }
            .maxByOrNull { it.length }

        return CardData(pan, expiryMonth, expiryYear, holderName)
    }

    private fun cancelAndFinish() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private data class CardData(
        val pan: String?,
        val expiryMonth: String?,
        val expiryYear: String?,
        val holderName: String?
    )

    companion object {
        const val EXTRA_PAN              = "scanned_pan"
        const val EXTRA_EXPIRY_MONTH     = "scanned_expiry_month"
        const val EXTRA_EXPIRY_YEAR      = "scanned_expiry_year"
        const val EXTRA_CARDHOLDER_NAME  = "scanned_cardholder_name"
        const val EXTRA_FRONT_IMAGE_PATH = "scanned_front_image_path"
        const val EXTRA_BACK_IMAGE_PATH  = "scanned_back_image_path"
    }
}
