package com.pass.hype

enum class Direction(val degrees: Int){
    NORTH(0), SOUTH(180), EAST(90), WEST(270)
}

fun main() {
    println(Direction.NORTH.ordinal)
}