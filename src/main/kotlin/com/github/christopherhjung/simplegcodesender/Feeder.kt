package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.connection.Connection
import java.io.*
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread


abstract class Feeder<T>{
    var closing = false
    val thread = thread(false) {
        while(true){
            try {
                loop()
            }catch (ignore: Exception){
                if(closing){
                    break
                }else{
                    reset()
                }
            }
        }
    }

    abstract fun reset()
    abstract fun loop()

    fun start(){
        thread.start()
    }

    fun interrupt(){
        thread.interrupt()
    }

    fun stop(){
        closing = true
        interrupt()
    }
}

abstract class InputFeeder<T>(val connection: Connection, val queue: BlockingQueue<T>, val context: FeederContext<T>) : Feeder<Reader>()
abstract class OutputFeeder<T>(val connection: Connection, val queue: BlockingQueue<T>, val context: FeederContext<T>) : Feeder<Writer>()

open class FrameInputFeeder(connection: Connection, queue: BlockingQueue<ByteArray>, context: FeederContext<ByteArray>) : InputFeeder<ByteArray>(connection, queue, context){
    private var inputStream: BufferedInputStream? = null

    override fun reset() {
        inputStream = null
    }

    override fun loop() {
        if(inputStream == null){
            inputStream = connection.requestInputStream(context).buffered()
        }

        inputStream?.apply {
            val length = read()
            val bytes = readNBytes(length)
            queue.offer(bytes)
        }
    }
}

class FrameOutputFeeder( connection: Connection, queue: BlockingQueue<ByteArray>, context: FeederContext<ByteArray> ) : OutputFeeder<ByteArray>(connection, queue, context){
    private var outputStream: BufferedOutputStream? = null

    override fun reset() {
        outputStream = null
    }

    override fun loop() {
        if(outputStream == null ){
            outputStream = connection.requestOutputStream(context).buffered()
        }

        val value = queue.take()

        outputStream?.apply{
            write(value.size)
            write(value)
        }
    }
}


class LineInputFeeder( connection: Connection, queue: BlockingQueue<String>, context: FeederContext<String>) : InputFeeder<String>(connection, queue, context){
    private var reader: BufferedReader? = null

    override fun reset() {
        reader = null
    }

    override fun loop() {
        if(reader == null){
            reader = connection.requestInputStream(context).bufferedReader()
        }

        val line = Utils.interruptableReadLine(reader!!)

        queue.offer(line)
    }
}

class LineOutputFeeder( connection: Connection, queue: BlockingQueue<String>, context: FeederContext<String>) : OutputFeeder<String>(connection, queue, context){
    private var writer: PrintWriter? = null

    override fun reset() {
        writer = null
    }

    override fun loop() {
        if(writer == null || writer?.checkError() == null){
            writer = PrintWriter(connection.requestOutputStream(context))
        }

        val value = queue.take()

        with(writer!!){
            write(value)
            write('\n'.code)
            flush()
        }
    }
}
