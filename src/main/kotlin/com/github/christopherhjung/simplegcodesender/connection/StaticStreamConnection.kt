package com.github.christopherhjung.simplegcodesender.connection

import com.github.christopherhjung.simplegcodesender.FeederContext
import java.io.InputStream
import java.io.OutputStream

open class StaticStreamConnection(val inputStream: InputStream, val outputStream: OutputStream) : Connection() {
    override val connected: Boolean = true

    override fun requestInputStream(context: FeederContext<*>): InputStream {
        return inputStream
    }

    override fun requestOutputStream(context: FeederContext<*>): OutputStream {
        return outputStream
    }
}
