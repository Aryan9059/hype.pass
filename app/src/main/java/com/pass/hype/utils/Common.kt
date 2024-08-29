package com.pass.hype.utils

fun String.capitalizeWords(delimiter: String = " ") =
    split(delimiter).joinToString(delimiter) { word ->

        val smallCaseWord = word.lowercase()
        smallCaseWord.replaceFirstChar(Char::titlecaseChar)

    }

