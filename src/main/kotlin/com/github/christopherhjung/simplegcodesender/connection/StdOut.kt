package com.github.christopherhjung.simplegcodesender.connection

import java.io.InputStream
import java.io.OutputStream

class StdOut : Connection() {
    override val connected: Boolean = true

    override fun requestInputStream(): InputStream {
        return InputStream.nullInputStream()
    }

    override fun requestOutputStream(): OutputStream {
        return System.out
    }
}
