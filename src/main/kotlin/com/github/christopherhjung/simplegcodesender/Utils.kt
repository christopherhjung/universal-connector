package com.github.christopherhjung.simplegcodesender

import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.IOException
import java.lang.RuntimeException
import java.lang.Thread.sleep
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utils {
    fun interruptableReadLine(reader: BufferedReader): String {
        val line: Pattern = Pattern.compile("^(.*)\\R")
        val result = StringBuilder()
        while(true) {
            try{
                val chr = reader.read()
                if (chr > -1){
                    result.append(chr.toChar())
                }else{
                    throw RuntimeException("end of stream")
                }

                val matcher = line.matcher(result.toString())
                if (Thread.interrupted()) throw InterruptedException()
                if(matcher.matches()){
                    return matcher.group(1)
                }
            }catch (e: IOException){
                if(e.message != "Underlying input stream returned zero bytes"){
                    throw e
                }
            }
        }
    }
}
