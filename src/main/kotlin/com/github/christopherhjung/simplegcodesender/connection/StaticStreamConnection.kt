package com.github.christopherhjung.simplegcodesender.connection

import java.io.InputStream
import java.io.OutputStream

open class StaticStreamConnection(val inputStream: InputStream, val outputStream: OutputStream) : Connection() {
    override val connected: Boolean = true

    override fun requestInputStream(): InputStream {
        return inputStream
    }

    override fun requestOutputStream(): OutputStream {
        return outputStream
    }
}
