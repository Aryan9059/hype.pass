package com.pass.hype.data.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Cards(
    @PrimaryKey(autoGenerate = true)
    val cardId: Int = 0,
    val cardHolder: String,
    val cardNumber: String,
    val expires: String,
    val cvv: String,
    val baseColor: String
)