package com.pass.hype.utils

val appList = listOf(
    "Amazon", "Apple", "ChatGPT", "Facebook", "GitHub", "Google", "Instagram",
    "LinkedIn", "Microsoft", "Netflix", "Pinterest", "Reddit", "Snapchat",
    "Spotify","TikTok", "X")

val cardList = listOf(
    "VISA", "MASTERCARD", "\uD83C\uDDFA\uD83C\uDDF8 EXPRESS", "OTHER")

fun String.capitalizeWords(delimiter: String = " ") =
    split(delimiter).joinToString(delimiter) { word ->

        val smallCaseWord = word.lowercase()
        smallCaseWord.replaceFirstChar(Char::titlecaseChar)

    }

fun generateStrongPassword(length: Int = 18): String {
    val upperCaseLetters = ('A'..'Z').toList()
    val lowerCaseLetters = ('a'..'z').toList()
    val numbers = ('0'..'9').toList()
    val specialCharacters = listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '+', '=', '[', ']', '?')

    val password = mutableListOf<Char>().apply {
        add(upperCaseLetters.random())
        add(lowerCaseLetters.random())
        add(numbers.random())
        add(specialCharacters.random())

        val remainingLength = length - 4
        val allCharacters = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters

        repeat(remainingLength) {
            add(allCharacters.random())
        }
    }
    return password.shuffled().joinToString("")
}

fun String.isStrongPassword(): Int {
    val hasUpperCase = any { it.isUpperCase() }
    val hasLowerCase = any { it.isLowerCase() }
    val hasDigits = any { it.isDigit() }
    val hasSpecialChar = any { !it.isLetterOrDigit() }

    var counter = 0

    if (hasUpperCase) counter++
    if (hasLowerCase) counter++
    if (hasDigits) counter++
    if (hasSpecialChar) counter++

    return counter
}

