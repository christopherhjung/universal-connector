package com.github.christopherhjung.simplegcodesender

import java.io.BufferedReader
import java.io.IOException
import java.util.regex.Pattern

object Utils {
    private  val pattern = Pattern.compile("^(.*)\\R")
    fun interruptableReadLine(reader: BufferedReader): String {
        val result = StringBuilder()
        while(true) {
            try{
                val chr = reader.read()
                if (chr > -1){
                    result.append(chr.toChar())
                }else{
                    throw RuntimeException("end of stream")
                }

                val matcher = pattern.matcher(result)
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
