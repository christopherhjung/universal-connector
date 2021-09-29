package com.github.christopherhjung.simplegcodesender

import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.IOException
import java.lang.Thread.sleep
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utils {
    fun interruptibleReadLine(reader: BufferedReader): String? {
        val line: Pattern = Pattern.compile("^(.*)\\R")
        val result = StringBuilder()
        while(true) {
            if (reader.ready()){
                val chr = reader.read()
                if (chr > -1){
                    result.append(chr.toChar())
                }else{
                    return null
                }

                val matcher = line.matcher(result.toString())
                if (Thread.interrupted()) throw InterruptedException()
                if(matcher.matches()){
                    return matcher.group(1)
                }
            }else{
                sleep(100)
            }
        }

        return result.toString()
    }
}
