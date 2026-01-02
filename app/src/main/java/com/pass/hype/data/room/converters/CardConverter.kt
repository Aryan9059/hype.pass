package com.pass.hype.data.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect. TypeToken
import com.pass.hype.data.room. model.CardNetwork
import com.pass.hype.data.room.model. CardSubType
import com.pass.hype.data.room.model.CardType

class CardConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromCardType(value: CardType): String = value.name

    @TypeConverter
    fun toCardType(value:  String): CardType = CardType.valueOf(value)

    @TypeConverter
    fun fromCardSubType(value: CardSubType): String = value.name

    @TypeConverter
    fun toCardSubType(value: String): CardSubType = CardSubType.valueOf(value)

    @TypeConverter
    fun fromCardNetwork(value: CardNetwork? ): String? = value?.name

    @TypeConverter
    fun toCardNetwork(value: String? ): CardNetwork? = value?.let { CardNetwork.valueOf(it) }

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value:  String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }
}