package com.pass.hype.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 2, // Updated version for new card format
    val createdAt: Long = System.currentTimeMillis(),
    val passwords: List<PasswordBackup>,
    val cards:  List<CardBackup>
)

@Serializable
data class PasswordBackup(
    val appName: String,
    val appIcon:  String,
    val email: String,
    val password: String,
    val editTime: String,
    val edited: Boolean,
    val notes: String
)

@Serializable
data class CardBackup(
    // Basic Info
    val cardName: String,
    val cardType: String,
    val cardSubType: String,

    // Card Details
    val cardNumber: String,
    val cardNumberMasked: String,
    val holderName: String,
    val expiryDate: String?  = null,
    val cvv: String?  = null,
    val issuer: String?  = null,
    val cardNetwork: String? = null,
    val issuingCountry: String = "India",

    // Additional Fields for ID Cards
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val fatherName: String?  = null,
    val issueDate: String? = null,
    val validFrom: String? = null,
    val validUntil: String?  = null,
    val authorityName: String? = null,

    // Organization
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val category: String? = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,

    // Custom Fields
    val customFields: Map<String, String> = emptyMap(),

    // Visual
    val baseColor:  String,
    val frontImageUri: String? = null,
    val backImageUri: String?  = null,

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt:  Long = System.currentTimeMillis(),
    val lastAccessedAt: Long?  = null,
    val accessCount: Int = 0
)

// Legacy card backup for migration from old format
@Serializable
data class LegacyCardBackup(
    val cardHolder: String,
    val cardNumber: String,
    val expires: String,
    val cvv: String,
    val baseColor: String
)

// Wrapper to handle both old and new formats
@Serializable
data class BackupDataV1(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val passwords: List<PasswordBackup>,
    val cards: List<LegacyCardBackup>
)