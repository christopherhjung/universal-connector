package com.github.christopherhjung.simplegcodesender

object Checksum {
    fun xor(str: String): Int {
        var checksum = 0
        for (data in str.toByteArray()) {
            checksum = checksum xor data.toInt()
        }
        return checksum
    }
}
