package com.github.christopherhjung.simplegcodesender.connection

import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class Loopback : StreamConnection(){
    val inputStream = PipedInputStream()
    val outputStream = PipedOutputStream(inputStream)
    override val connected: Boolean = true

    override fun open(){
    }

    override fun getInputStream() : InputStream {
        return inputStream
    }

    override fun getOutputStream() : OutputStream {
        return outputStream
    }
}
