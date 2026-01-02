package com.pass.hype.data.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Passwords")
data class Passwords(
    @PrimaryKey(autoGenerate = true)
    val passwordId: Int = 0,
    val appName: String,
    val appIcon: String,
    var email: String,
    var password: String,
    val editTime: String,
    val edited: Boolean,
    var notes: String
)