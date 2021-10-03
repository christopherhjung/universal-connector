package com.github.christopherhjung.simplegcodesender.connection

import java.io.InputStream
import java.io.OutputStream

open class Program(vararg val command: String) : StreamConnection(){
    private var process: Process? = null
    override val connected: Boolean = process?.isAlive ?: false

    override fun open(){
        process?.destroy()
        val builder = ProcessBuilder()
        process = builder.command(*command).start()
    }

    override fun getInputStream() : InputStream {
        return process!!.inputStream
    }

    override fun getOutputStream() : OutputStream {
        return process!!.outputStream
    }

    override fun close() {
    }
}
