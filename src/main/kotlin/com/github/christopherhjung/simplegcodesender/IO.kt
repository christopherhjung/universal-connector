package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.Utils.interruptibleReadLine
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.PrintWriter
import java.io.Writer
import java.lang.RuntimeException


interface Input {
    fun read() : String
}

interface Output {
    fun write(line: String)
}


class StreamInput(val connection: Connection) : Input {
    private var reader: BufferedReader? = null

    override fun read(): String {
        while(true){
            if(reader == null){
                reader = connection.requestInputStream().bufferedReader()
            }

            val line = interruptibleReadLine(reader!!)

            if(line == null){
                reader = null
                continue
            }

            return line
        }
    }
}

class EndInput : RuntimeException()

interface Sink{
    //fun send(line: String)
}

class StreamOutput(val connection: Connection) : Output, Sink {
    private var writer: PrintWriter? = null

    override fun write(line: String) {
        var retry = true
        while(retry){
            retry = false
            try{
                if(writer == null || writer?.checkError() == true){
                    writer = PrintWriter(connection.requestOutputStream())
                }

                with(writer!!){
                    write(line)
                    write('\n'.code)
                    flush()
                }
            }catch (e : Exception){
                writer = null
                retry = true
            }
        }
    }
}


