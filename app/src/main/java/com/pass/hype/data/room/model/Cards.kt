package com.pass.hype.data.room.model

import androidx. room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pass.hype.data.room.converters.CardConverters

@Entity(tableName = "cards")
@TypeConverters(CardConverters::class)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val cardId:  Int = 0,

    // Basic Info
    val cardName: String,
    val cardType: CardType,
    val cardSubType: CardSubType,

    // Card Details
    val cardNumber: String,
    val cardNumberMasked: String,
    val holderName: String,
    val expiryDate: String?  = null,
    val cvv: String?  = null,
    val issuer: String?  = null,
    val cardNetwork: CardNetwork?  = null,
    val issuingCountry:  String = "India",

    // Additional Fields for ID Cards
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val fatherName: String? = null,
    val issueDate: String? = null,
    val validFrom: String? = null,
    val validUntil: String?  = null,
    val authorityName: String? = null,

    // Organization
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val category: String?  = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,

    // Custom Fields
    val customFields: Map<String, String> = emptyMap(),

    // Visual
    val baseColor: String,
    val frontImageUri: String? = null,
    val backImageUri: String?  = null,

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt:  Long = System.currentTimeMillis(),
    val lastAccessedAt: Long?  = null,
    val accessCount: Int = 0
)

enum class CardType {
    FINANCIAL,
    GOVERNMENT_ID,
    MEMBERSHIP,
    TRANSIT,
    INSURANCE,
    STUDENT,
    EMPLOYEE,
    CUSTOM
}

enum class CardSubType {
    // Financial
    CREDIT_CARD,
    DEBIT_CARD,
    PREPAID_CARD,
    FOREX_CARD,
    VIRTUAL_CARD,
    UPI_CARD,
    CORPORATE_CARD,

    // Government ID
    AADHAAR,
    PAN,
    DRIVING_LICENSE,
    VOTER_ID,
    PASSPORT,
    RATION_CARD,
    AYUSHMAN_BHARAT,
    HEALTH_INSURANCE,

    // Membership
    GYM_MEMBERSHIP,
    LIBRARY_CARD,
    CLUB_MEMBERSHIP,
    LOYALTY_CARD,
    REWARD_CARD,

    // Transit
    METRO_CARD,
    BUS_PASS,
    FASTAG,

    // Insurance
    LIFE_INSURANCE,
    VEHICLE_INSURANCE,
    MEDICAL_INSURANCE,

    // Education/Work
    STUDENT_ID,
    EMPLOYEE_ID,

    // Others
    GIFT_CARD,
    EVENT_PASS,
    HOTEL_KEY,
    QR_CARD,
    CUSTOM
}

enum class CardNetwork {
    VISA,
    MASTERCARD,
    RUPAY,
    AMEX,
    DISCOVER,
    DINERS,
    MAESTRO,
    JCB,
    UNKNOWN
}