package com.github.christopherhjung.simplegcodesender.connection

import com.github.christopherhjung.simplegcodesender.InputFeeder
import com.github.christopherhjung.simplegcodesender.OutputFeeder
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue


abstract class Connection{
    abstract val connected: Boolean
    val input: InputFeeder = InputFeeder(this, LinkedBlockingQueue<String>())
    val output: OutputFeeder = OutputFeeder(this, LinkedBlockingQueue<String>())

    abstract fun requestInputStream() : InputStream
    abstract fun requestOutputStream() : OutputStream

    open fun close(){}

    fun start(){
        input.start()
        output.start()
    }

    fun stop(){
        input.stop()
        output.stop()
    }
}














