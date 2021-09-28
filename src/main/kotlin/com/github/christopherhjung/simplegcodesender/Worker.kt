package com.github.christopherhjung.simplegcodesender

import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.interrupted

class Worker(val inputStream: InputStream, val outputStream: OutputStream, val filters: List<Filter>) : Thread() {
    override fun run() {
        val reader = inputStream.bufferedReader()
        val writer = outputStream.bufferedWriter()
        while(!interrupted()){
            var line = reader.readLine() ?: return

            for( filter in filters ){
                line = filter.filter(line)
            }

            writer.append(line)
            writer.newLine()
            writer.flush()

            Thread.sleep(100)
        }
    }
}
