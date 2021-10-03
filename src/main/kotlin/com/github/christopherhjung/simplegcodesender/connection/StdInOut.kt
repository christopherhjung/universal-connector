package com.github.christopherhjung.simplegcodesender.connection

import java.io.InputStream
import java.io.OutputStream

class StdInOut : Connection() {
    override val connected: Boolean = true

    override fun requestInputStream(): InputStream {
        return System.`in`
    }

    override fun requestOutputStream(): OutputStream {
        return System.out
    }
}
