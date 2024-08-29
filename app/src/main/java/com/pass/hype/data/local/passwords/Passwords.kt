package com.pass.hype.data.local.passwords

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Passwords(
    @PrimaryKey(autoGenerate = true)
    val passwordId: Int = 0,
    val appName: String,
    val appIcon: String,
    var email: String,
    var password: String,
    val editTime: String,
    val edited: Boolean
)
