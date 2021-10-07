package com.github.christopherhjung.simplegcodesender.connection

import com.github.christopherhjung.simplegcodesender.FeederContext
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Semaphore

abstract class StreamConnection : Connection(){
    private val inputSemaphore = Semaphore(0)
    private val leaveSemaphore = Semaphore(0)
    private val enterSemaphore = Semaphore(0)

    abstract fun open()
    abstract fun getInputStream() : InputStream
    abstract fun getOutputStream() : OutputStream

    private var start = true

    override fun requestInputStream(context: FeederContext<*>): InputStream {
        if(start){
            context.output.interrupt()
            while(true){
                try{
                    inputSemaphore.acquire()
                    break
                }catch (e : InterruptedException){
                    Thread.interrupted()
                }
            }
            enterSemaphore.release()
            Thread.interrupted()
            open()
            leaveSemaphore.release()
        }
        return getInputStream()
    }

    override fun requestOutputStream(context: FeederContext<*>): OutputStream {
        if(start){
            inputSemaphore.release()
            context.input.interrupt()
            while(true){
                try{
                    enterSemaphore.acquire()
                    break
                }catch (e : InterruptedException){
                    Thread.interrupted()
                }
            }
            Thread.interrupted()
            leaveSemaphore.acquire()
        }
        return getOutputStream()
    }
}
