package com.pass.hype.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
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
    val cardHolder: String,
    val cardNumber: String,
    val expires: String,
    val cvv: String,
    val baseColor: String
)