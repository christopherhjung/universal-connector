package com.github.christopherhjung.simplegcodesender

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.*
import java.lang.RuntimeException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import javax.net.ServerSocketFactory
import javax.net.SocketFactory
import kotlin.system.measureTimeMillis

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

class StdInOutConnection : Connection() {
    override val connected: Boolean = true

    override fun requestInputStream(): InputStream {
        return System.`in`
    }

    override fun requestOutputStream(): OutputStream {
        return System.out
    }
}

class StdOutConnection : Connection() {
    override val connected: Boolean = true

    override fun requestInputStream(): InputStream {
        return ByteArrayInputStream("".toByteArray())
    }

    override fun requestOutputStream(): OutputStream {
        return System.out
    }
}

abstract class StreamConnection : Connection(){
    private val inputSemaphore = Semaphore(0)
    private val outputSemaphore = Semaphore(0)
    private val enterSemaphore = Semaphore(0)

    abstract fun open()
    abstract fun getInputStream() : InputStream
    abstract fun getOutputStream() : OutputStream

    private var start = true

    override fun requestInputStream(): InputStream {
        if(start){
            if(!inputSemaphore.tryAcquire(2000, TimeUnit.MILLISECONDS)){
                output.interrupt()
                inputSemaphore.acquire()
            }
            enterSemaphore.release()

            open()
            outputSemaphore.release()
        }
        return getInputStream()
    }

    override fun requestOutputStream(): OutputStream {
        if(start){
            inputSemaphore.release()
            if(!enterSemaphore.tryAcquire(2000, TimeUnit.MILLISECONDS)){
                input.interrupt()
                enterSemaphore.acquire()
            }
            outputSemaphore.acquire()
        }
        return getOutputStream()
    }
}

class ServerConnection(port: Int) : StreamConnection(){
    private val server = ServerSocketFactory.getDefault().createServerSocket(port)
    private var socket: Socket? = null
    override val connected: Boolean = socket?.isConnected ?: false

    override fun open(){
        socket = server.accept()
    }

    override fun getInputStream() : InputStream {
        return socket!!.getInputStream()
    }

    override fun getOutputStream() : OutputStream {
        return socket!!.getOutputStream()
    }

    override fun close() {
        socket?.close()
        server.close()
    }
}

class SerialConnection(val name: String) : StreamConnection(){
    private var port: SerialPort? = null
    override val connected: Boolean = port?.isOpen ?: false

    override fun open(){
        port = SerialPort.getCommPort(name).apply {
            baudRate = 115200
            setComPortTimeouts( TIMEOUT_READ_BLOCKING, 1000000000,0)
            openPort()
        }
    }

    override fun getInputStream() : InputStream {
        return port!!.inputStream
    }

    override fun getOutputStream() : OutputStream {
        return port!!.outputStream
    }

    override fun close() {
        port?.closePort()
    }
}

class Loopback : StreamConnection(){
    val inputStream = PipedInputStream()
    val outputStream = PipedOutputStream(inputStream)
    override val connected: Boolean = true

    override fun open(){
    }

    override fun getInputStream() : InputStream {
        return inputStream
    }

    override fun getOutputStream() : OutputStream {
        return outputStream
    }
}

class ClientConnection(val host: String, val port: Int) : StreamConnection(){
    private var socket: Socket? = null
    override val connected: Boolean = socket?.isConnected ?: false

    override fun open(){
        socket = SocketFactory.getDefault().createSocket(host, port)
    }

    override fun getInputStream() : InputStream {
        return socket!!.getInputStream()
    }

    override fun getOutputStream() : OutputStream {
        return socket!!.getOutputStream()
    }

    override fun close() {
        socket?.close()
    }
}

