package com.github.christopherhjung.simplegcodesender.connection

import com.github.christopherhjung.simplegcodesender.*
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue


abstract class Connection{
    abstract val connected: Boolean

    val factory = LineFeederFactory()
    //val input: InputFeeder<String> = factory.createInputFeeder(this, LinkedBlockingQueue())
    //val output: OutputFeeder<String> = factory.createOutputFeeder(this, LinkedBlockingQueue())

    abstract fun requestInputStream(context: FeederContext<*>) : InputStream
    abstract fun requestOutputStream(context: FeederContext<*>) : OutputStream

    open fun close(){}
}














