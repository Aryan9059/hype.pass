package com.pass.hype.utils

data class AppList(
    val name: String,
    val bgColor: String,
    val logoColor: String
)

val appList = listOf(
    "Amazon", "Apple", "ChatGPT", "Facebook", "GitHub", "Google", "Instagram",
    "LinkedIn", "Microsoft", "Netflix", "Pinterest", "Reddit", "Snapchat",
    "Spotify","TikTok", "X")

val cardList = listOf(
    "VISA", "MASTERCARD", "\uD83C\uDDFA\uD83C\uDDF8 EXPRESS", "OTHER")

val appListWithColors = listOf(
    AppList("Amazon", "#ffffff", "#000000"),
    AppList("Apple", "#000000", "#ffffff"),
    AppList("ChatGPT", "#28a08c", "#ffffff"),
    AppList("Facebook", "#0866ff", "#ffffff"),
    AppList("GitHub", "#000000", "#ffffff"),
    AppList("Google", "#4285f4", "#ffffff"),
    AppList("Instagram", "#dd2a7b", "#ffffff"),
    AppList("LinkedIn", "#0077b5", "#ffffff"),
    AppList("Microsoft", "#f65314", "#ffffff"),
    AppList("Netflix", "#141414", "#e50914"),
    AppList("Pinterest", "#bd081c", "#ffffff"),
    AppList("Reddit", "#ff4500", "#ffffff"),
    AppList("Snapchat", "#fffc00", "#000000"),
    AppList("Spotify", "#1d8954", "#191414"),
    AppList("TikTok", "#00f2ea", "#000000"),
    AppList("X", "#000000", "#ffffff"),
)