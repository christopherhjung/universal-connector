package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.connection.Connection
import java.io.BufferedReader
import java.io.PrintWriter
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread


abstract class Feeder<T>{
    protected var context: T? = null
    var closing = false
    val thread = thread(false) {
        while(true){
            try {
                impl()
            }catch (ignore: Exception){
                if(closing){
                    break
                }else{
                    context = null
                }
            }
        }
    }

    abstract fun impl()

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

class InputFeeder(val connection: Connection, val queue: BlockingQueue<String>) : Feeder<BufferedReader>(){
    override fun impl() {
        if(context == null){
            context = connection.requestInputStream().bufferedReader()
        }

        val line = Utils.interruptableReadLine(context!!)

        queue.offer(line)
    }
}

class OutputFeeder(val connection: Connection, val queue: BlockingQueue<String>) : Feeder<PrintWriter>(){
    override fun impl() {
        if(context == null || context?.checkError() == null){
            context = PrintWriter(connection.requestOutputStream())
        }

        val value = queue.take()

        with(context!!){
            write(value)
            write('\n'.code)
            flush()
        }
    }
}
