package com.pass.hype.utils

import java.security.SecureRandom
import kotlin.math.ln
import kotlin.math.log2

/**
 * Password strength levels with associated properties
 */
enum class PasswordStrength(
    val label: String,
    val score: Int,
    val color: Long // ARGB color
) {
    VERY_WEAK("Very Weak", 0, 0xFFE53935),
    WEAK("Weak", 1, 0xFFFF7043),
    FAIR("Fair", 2, 0xFFFFA726),
    GOOD("Good", 3, 0xFF66BB6A),
    STRONG("Strong", 4, 0xFF43A047),
    VERY_STRONG("Very Strong", 5, 0xFF2E7D32)
}

/**
 * Configuration for password generation
 */
data class PasswordConfig(
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols:  Boolean = true,
    val excludeAmbiguous: Boolean = false, // Exclude 0, O, l, 1, I, etc.
    val excludeSimilar: Boolean = false,   // Exclude (), {}, [], <>, etc.
    val customSymbols: String?  = null,
    val mustIncludeEach: Boolean = true    // Must include at least one of each selected type
)

object PasswordUtils {

    private val secureRandom = SecureRandom()

    // Character sets
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>? /"
    private const val SYMBOLS_SAFE = "!@#$%^&*_+-=?"

    // Ambiguous characters
    private const val AMBIGUOUS = "0O1lI|"
    private const val SIMILAR_PAIRS = "(){}[]<>"

    // Common passwords list (top 100 most common)
    private val COMMON_PASSWORDS = setOf(
        "password", "123456", "12345678", "qwerty", "abc123", "monkey", "1234567",
        "letmein", "trustno1", "dragon", "baseball", "iloveyou", "master", "sunshine",
        "ashley", "bailey", "shadow", "123123", "654321", "superman", "qazwsx",
        "michael", "football", "password1", "password123", "welcome", "jesus",
        "ninja", "mustang", "password1234", "admin", "login", "princess", "solo",
        "passw0rd", "starwars", "121212", "000000", "access", "flower", "hottie",
        "loveme", "zaq1zaq1", "hello", "charlie", "donald", "password12", "qwerty123",
        "1q2w3e4r", "1qaz2wsx", "qwertyuiop", "administrator", "root", "toor"
    )

    // Common patterns
    private val KEYBOARD_PATTERNS = listOf(
        "qwerty", "asdf", "zxcv", "qazwsx", "1qaz", "2wsx",
        "1234", "2345", "3456", "4567", "5678", "6789", "7890",
        "abcd", "bcde", "cdef", "defg"
    )

    /**
     * Generate a strong password based on configuration
     */
    fun generatePassword(config: PasswordConfig = PasswordConfig()): String {
        val charPool = buildCharacterPool(config)

        if (charPool.isEmpty()) {
            return generatePassword(PasswordConfig()) // Fallback to default
        }

        val password = StringBuilder()
        val requiredChars = mutableListOf<Char>()

        // Ensure at least one character from each selected category
        if (config.mustIncludeEach) {
            if (config.includeUppercase) {
                requiredChars.add(getRandomChar(filterAmbiguous(UPPERCASE, config)))
            }
            if (config. includeLowercase) {
                requiredChars.add(getRandomChar(filterAmbiguous(LOWERCASE, config)))
            }
            if (config.includeNumbers) {
                requiredChars. add(getRandomChar(filterAmbiguous(NUMBERS, config)))
            }
            if (config. includeSymbols) {
                val symbols = config.customSymbols ?: if (config.excludeSimilar) SYMBOLS_SAFE else SYMBOLS
                requiredChars.add(getRandomChar(symbols))
            }
        }

        // Fill remaining length with random characters
        val remainingLength = config.length - requiredChars.size
        repeat(remainingLength) {
            password.append(charPool[secureRandom.nextInt(charPool.length)])
        }

        // Add required characters at random positions
        requiredChars.forEach { char ->
            val position = secureRandom.nextInt(password.length + 1)
            password.insert(position, char)
        }

        // Shuffle the final password for extra randomness
        return shuffleString(password.toString())
    }

    /**
     * Generate a memorable passphrase
     */
    fun generatePassphrase(
        wordCount: Int = 4,
        separator: String = "-",
        capitalize: Boolean = true,
        includeNumber: Boolean = true
    ): String {
        val words = WORD_LIST.shuffled(secureRandom).take(wordCount).map { word ->
            if (capitalize) word.replaceFirstChar { it.uppercase() } else word
        }. toMutableList()

        if (includeNumber) {
            val position = secureRandom.nextInt(words.size + 1)
            val number = secureRandom.nextInt(100).toString()
            words.add(position, number)
        }

        return words.joinToString(separator)
    }

    /**
     * Analyze password strength with detailed feedback
     */
    fun analyzePassword(password: String): PasswordAnalysis {
        if (password.isEmpty()) {
            return PasswordAnalysis(
                strength = PasswordStrength.VERY_WEAK,
                score = 0,
                entropy = 0.0,
                crackTime = "Instant",
                feedback = listOf("Password is empty"),
                suggestions = listOf("Enter a password")
            )
        }

        val feedback = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        var score = 0

        // Length analysis
        val lengthScore = when {
            password. length >= 20 -> 30
            password.length >= 16 -> 25
            password.length >= 12 -> 20
            password.length >= 10 -> 15
            password.length >= 8 -> 10
            password.length >= 6 -> 5
            else -> 0
        }
        score += lengthScore

        if (password.length < 8) {
            feedback.add("Password is too short")
            suggestions.add("Use at least 8 characters")
        } else if (password.length < 12) {
            suggestions.add("Consider using 12+ characters for better security")
        }

        // Character variety analysis
        val hasUppercase = password. any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigits = password.any { it.isDigit() }
        val hasSymbols = password.any { ! it.isLetterOrDigit() }

        var varietyScore = 0
        var charsetSize = 0

        if (hasUppercase) {
            varietyScore += 10
            charsetSize += 26
        } else {
            suggestions.add("Add uppercase letters (A-Z)")
        }

        if (hasLowercase) {
            varietyScore += 10
            charsetSize += 26
        } else {
            suggestions.add("Add lowercase letters (a-z)")
        }

        if (hasDigits) {
            varietyScore += 10
            charsetSize += 10
        } else {
            suggestions.add("Add numbers (0-9)")
        }

        if (hasSymbols) {
            varietyScore += 15
            charsetSize += 32
        } else {
            suggestions.add("Add special characters (! @#$%)")
        }

        score += varietyScore

        // Entropy calculation
        val entropy = if (charsetSize > 0) {
            password.length * log2(charsetSize.toDouble())
        } else {
            0.0
        }

        // Penalize common passwords
        if (COMMON_PASSWORDS. contains(password. lowercase())) {
            score -= 50
            feedback.add("This is a commonly used password")
            suggestions.add("Choose a unique password")
        }

        // Penalize keyboard patterns
        val lowerPassword = password.lowercase()
        KEYBOARD_PATTERNS. forEach { pattern ->
            if (lowerPassword.contains(pattern)) {
                score -= 10
                feedback.add("Contains keyboard pattern: $pattern")
            }
        }

        // Penalize repeated characters
        val repeatedChars = findRepeatedSequences(password)
        if (repeatedChars > 0) {
            score -= repeatedChars * 5
            feedback.add("Contains repeated characters")
            suggestions.add("Avoid repeating characters")
        }

        // Penalize sequential characters
        val sequentialScore = findSequentialChars(password)
        if (sequentialScore > 0) {
            score -= sequentialScore * 3
            feedback. add("Contains sequential characters")
            suggestions. add("Avoid sequential patterns like 'abc' or '123'")
        }

        // Bonus for mixed case in middle
        if (password.drop(1).dropLast(1).any { it.isUpperCase() }) {
            score += 5
        }

        // Bonus for symbols in middle
        if (password.drop(1).dropLast(1).any { ! it.isLetterOrDigit() }) {
            score += 5
        }

        // Normalize score
        score = score.coerceIn(0, 100)

        // Determine strength level
        val strength = when {
            score >= 80 -> PasswordStrength.VERY_STRONG
            score >= 65 -> PasswordStrength.STRONG
            score >= 50 -> PasswordStrength.GOOD
            score >= 35 -> PasswordStrength.FAIR
            score >= 20 -> PasswordStrength.WEAK
            else -> PasswordStrength. VERY_WEAK
        }

        // Estimate crack time
        val crackTime = estimateCrackTime(entropy)

        return PasswordAnalysis(
            strength = strength,
            score = score,
            entropy = entropy,
            crackTime = crackTime,
            feedback = feedback,
            suggestions = suggestions. take(3), // Limit to 3 suggestions
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasDigits = hasDigits,
            hasSymbols = hasSymbols,
            length = password.length
        )
    }

    /**
     * Quick strength check (for backward compatibility)
     */
    fun getPasswordStrength(password: String): String {
        return analyzePassword(password).strength.label
    }

    // Helper functions

    private fun buildCharacterPool(config: PasswordConfig): String {
        val pool = StringBuilder()

        if (config.includeUppercase) {
            pool.append(filterAmbiguous(UPPERCASE, config))
        }
        if (config.includeLowercase) {
            pool.append(filterAmbiguous(LOWERCASE, config))
        }
        if (config.includeNumbers) {
            pool.append(filterAmbiguous(NUMBERS, config))
        }
        if (config.includeSymbols) {
            val symbols = config.customSymbols ?:  if (config.excludeSimilar) SYMBOLS_SAFE else SYMBOLS
            pool.append(symbols)
        }

        return pool.toString()
    }

    private fun filterAmbiguous(chars: String, config:  PasswordConfig): String {
        return if (config.excludeAmbiguous) {
            chars.filter { it !in AMBIGUOUS }
        } else {
            chars
        }
    }

    private fun getRandomChar(chars: String): Char {
        return chars[secureRandom.nextInt(chars.length)]
    }

    private fun shuffleString(input: String): String {
        val chars = input.toCharArray()
        for (i in chars. indices. reversed()) {
            val j = secureRandom.nextInt(i + 1)
            val temp = chars[i]
            chars[i] = chars[j]
            chars[j] = temp
        }
        return String(chars)
    }

    private fun findRepeatedSequences(password: String): Int {
        var count = 0
        var i = 0
        while (i < password.length - 2) {
            if (password[i] == password[i + 1] && password[i + 1] == password[i + 2]) {
                count++
                i += 3
            } else {
                i++
            }
        }
        return count
    }

    private fun findSequentialChars(password: String): Int {
        var count = 0
        for (i in 0 until password. length - 2) {
            val c1 = password[i]. code
            val c2 = password[i + 1]. code
            val c3 = password[i + 2]. code

            // Check ascending sequence
            if (c2 - c1 == 1 && c3 - c2 == 1) {
                count++
            }
            // Check descending sequence
            if (c1 - c2 == 1 && c2 - c3 == 1) {
                count++
            }
        }
        return count
    }

    private fun estimateCrackTime(entropy:  Double): String {
        // Assuming 10 billion guesses per second (modern GPU)
        val guessesPerSecond = 10_000_000_000.0
        val possibleCombinations = Math.pow(2.0, entropy)
        val seconds = possibleCombinations / guessesPerSecond / 2 // Average case

        return when {
            seconds < 1 -> "Instant"
            seconds < 60 -> "${seconds. toLong()} seconds"
            seconds < 3600 -> "${(seconds / 60).toLong()} minutes"
            seconds < 86400 -> "${(seconds / 3600).toLong()} hours"
            seconds < 2592000 -> "${(seconds / 86400).toLong()} days"
            seconds < 31536000 -> "${(seconds / 2592000).toLong()} months"
            seconds < 31536000 * 100 -> "${(seconds / 31536000).toLong()} years"
            seconds < 31536000 * 1000 -> "${(seconds / 31536000).toLong()} years"
            else -> "Centuries"
        }
    }

    // Word list for passphrase generation
    private val WORD_LIST = listOf(
        "apple", "banana", "cherry", "dragon", "eagle", "falcon", "guitar",
        "hammer", "island", "jungle", "knight", "lemon", "mountain", "nebula",
        "ocean", "piano", "quantum", "rocket", "sunset", "thunder", "umbrella",
        "violet", "whisper", "xylophone", "yellow", "zebra", "anchor", "breeze",
        "crystal", "diamond", "ember", "forest", "glacier", "harbor", "ivory",
        "jasmine", "kingdom", "lantern", "marble", "ninja", "orbit", "phoenix",
        "quartz", "rainbow", "silver", "tiger", "unity", "velvet", "wonder"
    )
}

/**
 * Detailed password analysis result
 */
data class PasswordAnalysis(
    val strength: PasswordStrength,
    val score: Int,
    val entropy: Double,
    val crackTime:  String,
    val feedback: List<String>,
    val suggestions: List<String>,
    val hasUppercase: Boolean = false,
    val hasLowercase:  Boolean = false,
    val hasDigits: Boolean = false,
    val hasSymbols: Boolean = false,
    val length: Int = 0
)

// Extension function for backward compatibility
fun String. getPasswordStrength(): String = PasswordUtils.getPasswordStrength(this)