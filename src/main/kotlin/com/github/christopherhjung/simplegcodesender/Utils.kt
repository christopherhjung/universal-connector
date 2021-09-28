package com.github.christopherhjung.simplegcodesender

import java.io.BufferedReader
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utils {
    @Throws(InterruptedException::class, IOException::class)
    fun interruptibleReadLine(reader: BufferedReader): String {
        val line: Pattern = Pattern.compile("^(.*)\\R")
        var matcher: Matcher
        var interrupted = false
        val result = StringBuilder()
        var chr = -1
        do {
            if (reader.ready()) chr = reader.read()
            if (chr > -1) result.append(chr.toChar())
            matcher = line.matcher(result.toString())
            interrupted = Thread.interrupted() // resets flag, call only once
        } while (!interrupted && !matcher.matches())
        if (interrupted) throw InterruptedException()
        return if (matcher.matches()) matcher.group(1) else ""
    }
}
