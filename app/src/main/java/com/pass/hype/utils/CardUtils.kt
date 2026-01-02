package com.pass.hype.utils

import com.pass.hype.data.room. model.Card
import com.pass.hype.data.room.model.CardNetwork
import com.pass.hype.data.room. model.CardSubType
import com.pass.hype.data.room.model. CardType
import java.text. SimpleDateFormat
import java.util.Calendar
import java.util. Locale

object CardUtils {

    // Card network detection from number
    fun detectCardNetwork(cardNumber: String): CardNetwork {
        val cleaned = cardNumber.replace(" ", "").replace("-", "")

        return when {
            cleaned.isEmpty() -> CardNetwork.UNKNOWN

            // Visa:  Starts with 4
            cleaned. startsWith("4") -> CardNetwork. VISA

            // Mastercard:  51-55 or 2221-2720
            cleaned.length >= 2 && (
                    cleaned. substring(0, 2).toIntOrNull() in 51..55 ||
                            (cleaned.length >= 4 && cleaned.substring(0, 4).toIntOrNull() in 2221.. 2720)
                    ) -> CardNetwork.MASTERCARD

            // RuPay: 60, 65, 81, 82, 508
            cleaned.startsWith("60") || cleaned.startsWith("65") ||
                    cleaned.startsWith("81") || cleaned.startsWith("82") ||
                    cleaned.startsWith("508") -> CardNetwork.RUPAY

            // Amex: 34 or 37
            cleaned.startsWith("34") || cleaned.startsWith("37") -> CardNetwork.AMEX

            // Discover: 6011, 644-649, 65
            cleaned.startsWith("6011") || cleaned.startsWith("65") ||
                    (cleaned.length >= 3 && cleaned.substring(0, 3).toIntOrNull() in 644..649) -> CardNetwork.DISCOVER

            // Diners: 36, 38, 300-305
            cleaned. startsWith("36") || cleaned.startsWith("38") ||
                    (cleaned.length >= 3 && cleaned.substring(0, 3).toIntOrNull() in 300..305) -> CardNetwork.DINERS

            // Maestro: 5018, 5020, 5038, 6304, 6759, 6761, 6763
            cleaned.startsWith("5018") || cleaned.startsWith("5020") ||
                    cleaned.startsWith("5038") || cleaned.startsWith("6304") ||
                    cleaned.startsWith("6759") || cleaned.startsWith("6761") ||
                    cleaned.startsWith("6763") -> CardNetwork.MAESTRO

            // JCB: 3528-3589
            cleaned.length >= 4 && cleaned.substring(0, 4).toIntOrNull() in 3528..3589 -> CardNetwork.JCB

            else -> CardNetwork. UNKNOWN
        }
    }

    // Mask card number
    fun maskCardNumber(cardNumber: String, visibleDigits: Int = 4): String {
        val cleaned = cardNumber.replace(" ", "").replace("-", "")
        if (cleaned.length <= visibleDigits) return cleaned

        val masked = "•".repeat(cleaned.length - visibleDigits)
        val visible = cleaned.takeLast(visibleDigits)

        return "$masked$visible"
    }

    // Format card number with spaces
    fun formatCardNumber(cardNumber:  String): String {
        val cleaned = cardNumber.replace(" ", "").replace("-", "")
        return cleaned.chunked(4).joinToString(" ")
    }

    // Validate Aadhaar format
    fun isValidAadhaar(number: String): Boolean {
        val cleaned = number.replace(" ", "").replace("-", "")
        return cleaned.length == 12 && cleaned.all { it.isDigit() } && ! cleaned.startsWith("0") && !cleaned.startsWith("1")
    }

    // Validate PAN format
    fun isValidPAN(pan: String): Boolean {
        val regex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
        return pan.uppercase().matches(regex)
    }

    // Validate driving license format (varies by state)
    fun isValidDrivingLicense(dl: String): Boolean {
        val cleaned = dl.uppercase().replace(" ", "").replace("-", "")
        // Format: SS-RR-YYYY-NNNNNNN (State-RTO-Year-Number)
        return cleaned.length in 13..16 && cleaned.take(2).all { it.isLetter() }
    }

    // Validate Voter ID format
    fun isValidVoterId(voterId: String): Boolean {
        val regex = Regex("^[A-Z]{3}[0-9]{7}$")
        return voterId.uppercase().matches(regex)
    }

    // Validate passport format
    fun isValidPassport(passport: String): Boolean {
        val regex = Regex("^[A-Z][0-9]{7}$")
        return passport.uppercase().matches(regex)
    }

    // Detect card subtype from number/format
    fun detectCardSubType(input: String): CardSubType?  {
        val cleaned = input.uppercase().replace(" ", "").replace("-", "")

        return when {
            isValidAadhaar(cleaned) -> CardSubType.AADHAAR
            isValidPAN(cleaned) -> CardSubType. PAN
            isValidVoterId(cleaned) -> CardSubType. VOTER_ID
            isValidPassport(cleaned) -> CardSubType.PASSPORT
            isValidDrivingLicense(cleaned) -> CardSubType. DRIVING_LICENSE
            cleaned.all { it.isDigit() } && cleaned.length == 16 -> CardSubType.DEBIT_CARD
            else -> null
        }
    }

    // Check if card is expiring soon
    fun isExpiringSoon(expiryDate: String?, daysThreshold: Int = 30): Boolean {
        if (expiryDate. isNullOrBlank()) return false

        try {
            val parts = expiryDate.split("/")
            if (parts.size != 2) return false

            val month = parts[0]. toIntOrNull() ?: return false
            val year = parts[1].toIntOrNull() ?: return false

            val fullYear = if (year < 100) 2000 + year else year

            val expiry = Calendar.getInstance().apply {
                set(Calendar. YEAR, fullYear)
                set(Calendar. MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            val today = Calendar.getInstance()
            val threshold = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, daysThreshold)
            }

            return expiry.after(today) && expiry.before(threshold)
        } catch (e: Exception) {
            return false
        }
    }

    // Check if card is expired
    fun isExpired(expiryDate: String?): Boolean {
        if (expiryDate.isNullOrBlank()) return false

        try {
            val parts = expiryDate.split("/")
            if (parts. size != 2) return false

            val month = parts[0].toIntOrNull() ?: return false
            val year = parts[1].toIntOrNull() ?: return false

            val fullYear = if (year < 100) 2000 + year else year

            val expiry = Calendar.getInstance().apply {
                set(Calendar.YEAR, fullYear)
                set(Calendar. MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            return Calendar.getInstance().after(expiry)
        } catch (e: Exception) {
            return false
        }
    }

    // Get days until expiry
    fun getDaysUntilExpiry(expiryDate: String? ): Int? {
        if (expiryDate. isNullOrBlank()) return null

        try {
            val parts = expiryDate. split("/")
            if (parts.size != 2) return null

            val month = parts[0].toIntOrNull() ?: return null
            val year = parts[1]. toIntOrNull() ?: return null

            val fullYear = if (year < 100) 2000 + year else year

            val expiry = Calendar.getInstance().apply {
                set(Calendar.YEAR, fullYear)
                set(Calendar. MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar. DAY_OF_MONTH))
            }

            val today = Calendar.getInstance()
            val diff = expiry.timeInMillis - today.timeInMillis

            return (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            return null
        }
    }

    // Generate random card colors
    val cardColors = listOf(
        "#FF1A1A2E", "#FF16213E", "#FF0F3460", "#FF533483",
        "#FF2C3E50", "#FF34495E", "#FF1ABC9C", "#FF16A085",
        "#FF27AE60", "#FF2ECC71", "#FF3498DB", "#FF2980B9",
        "#FF9B59B6", "#FF8E44AD", "#FFE74C3C", "#FFC0392B",
        "#FFE67E22", "#FFD35400", "#FFF39C12", "#FFF1C40F"
    )

    fun getRandomCardColor(): String = cardColors. random()

    // Get card type display name
    fun getCardTypeDisplayName(cardType: CardType): String {
        return when (cardType) {
            CardType.FINANCIAL -> "Financial Card"
            CardType.GOVERNMENT_ID -> "Government ID"
            CardType. MEMBERSHIP -> "Membership Card"
            CardType.TRANSIT -> "Transit Card"
            CardType.INSURANCE -> "Insurance Card"
            CardType. STUDENT -> "Student ID"
            CardType. EMPLOYEE -> "Employee ID"
            CardType.CUSTOM -> "Custom Card"
        }
    }

    // Get card subtype display name
    fun getCardSubTypeDisplayName(subType: CardSubType): String {
        return when (subType) {
            CardSubType.CREDIT_CARD -> "Credit Card"
            CardSubType. DEBIT_CARD -> "Debit Card"
            CardSubType.PREPAID_CARD -> "Prepaid Card"
            CardSubType.FOREX_CARD -> "Forex Card"
            CardSubType. VIRTUAL_CARD -> "Virtual Card"
            CardSubType. UPI_CARD -> "UPI Card"
            CardSubType.CORPORATE_CARD -> "Corporate Card"
            CardSubType. AADHAAR -> "Aadhaar Card"
            CardSubType.PAN -> "PAN Card"
            CardSubType.DRIVING_LICENSE -> "Driving License"
            CardSubType.VOTER_ID -> "Voter ID"
            CardSubType. PASSPORT -> "Passport"
            CardSubType. RATION_CARD -> "Ration Card"
            CardSubType.AYUSHMAN_BHARAT -> "Ayushman Bharat"
            CardSubType.HEALTH_INSURANCE -> "Health Insurance"
            CardSubType.GYM_MEMBERSHIP -> "Gym Membership"
            CardSubType.LIBRARY_CARD -> "Library Card"
            CardSubType. CLUB_MEMBERSHIP -> "Club Membership"
            CardSubType.LOYALTY_CARD -> "Loyalty Card"
            CardSubType.REWARD_CARD -> "Reward Card"
            CardSubType. METRO_CARD -> "Metro Card"
            CardSubType.BUS_PASS -> "Bus Pass"
            CardSubType. FASTAG -> "FASTag"
            CardSubType.LIFE_INSURANCE -> "Life Insurance"
            CardSubType. VEHICLE_INSURANCE -> "Vehicle Insurance"
            CardSubType. MEDICAL_INSURANCE -> "Medical Insurance"
            CardSubType. STUDENT_ID -> "Student ID"
            CardSubType.EMPLOYEE_ID -> "Employee ID"
            CardSubType.GIFT_CARD -> "Gift Card"
            CardSubType.EVENT_PASS -> "Event Pass"
            CardSubType. HOTEL_KEY -> "Hotel Key Card"
            CardSubType.QR_CARD -> "QR Card"
            CardSubType.CUSTOM -> "Custom"
        }
    }

    // Get card subtypes for a card type
    fun getSubTypesForType(cardType: CardType): List<CardSubType> {
        return when (cardType) {
            CardType.FINANCIAL -> listOf(
                CardSubType. CREDIT_CARD, CardSubType. DEBIT_CARD, CardSubType. PREPAID_CARD,
                CardSubType.FOREX_CARD, CardSubType.VIRTUAL_CARD, CardSubType.UPI_CARD,
                CardSubType. CORPORATE_CARD
            )
            CardType.GOVERNMENT_ID -> listOf(
                CardSubType. AADHAAR, CardSubType.PAN, CardSubType.DRIVING_LICENSE,
                CardSubType.VOTER_ID, CardSubType.PASSPORT, CardSubType. RATION_CARD,
                CardSubType.AYUSHMAN_BHARAT, CardSubType. HEALTH_INSURANCE
            )
            CardType.MEMBERSHIP -> listOf(
                CardSubType.GYM_MEMBERSHIP, CardSubType. LIBRARY_CARD, CardSubType. CLUB_MEMBERSHIP,
                CardSubType.LOYALTY_CARD, CardSubType.REWARD_CARD
            )
            CardType.TRANSIT -> listOf(
                CardSubType. METRO_CARD, CardSubType.BUS_PASS, CardSubType. FASTAG
            )
            CardType.INSURANCE -> listOf(
                CardSubType. LIFE_INSURANCE, CardSubType. VEHICLE_INSURANCE, CardSubType. MEDICAL_INSURANCE
            )
            CardType.STUDENT -> listOf(CardSubType.STUDENT_ID)
            CardType. EMPLOYEE -> listOf(CardSubType. EMPLOYEE_ID)
            CardType. CUSTOM -> listOf(
                CardSubType. GIFT_CARD, CardSubType.EVENT_PASS, CardSubType.HOTEL_KEY,
                CardSubType.QR_CARD, CardSubType.CUSTOM
            )
        }
    }
}