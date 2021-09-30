package com.github.christopherhjung.simplegcodesender

import java.io.BufferedReader
import java.io.File
import java.io.PrintWriter
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread


class InputFeeder(connection: Connection, val queue: BlockingQueue<String>){
    private var reader: BufferedReader? = null
    var closing = false
    val thread = thread(false){
        while(true){
            try {
                if(reader == null){
                    reader = connection.requestInputStream().bufferedReader()
                }

                val line = Utils.interruptableReadLine(reader!!)

                queue.offer(line)
            }catch (ignore: Exception){
                if(closing){
                    break
                }else{
                    reader = null
                }
            }
        }
    }

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

class OutputFeeder(connection: Connection, val queue: BlockingQueue<String>){
    private var writer: PrintWriter? = null
    var closing = false
    val thread = thread(false) {
        while(true){
            try {
                if(writer == null || writer?.checkError() == null){
                    writer = PrintWriter(connection.requestOutputStream())
                }

                val value = queue.take()

                with(writer!!){
                    write(value)
                    write('\n'.code)
                    flush()
                }
            }catch (ignore: Exception){
                if(closing){
                    break
                }else{
                    writer = null
                }
            }
        }
    }

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
