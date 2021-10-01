package com.github.christopherhjung.simplegcodesender

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING
import java.io.*
import java.net.Socket
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import javax.net.ServerSocketFactory
import javax.net.SocketFactory


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
    private val leaveSemaphore = Semaphore(0)
    private val enterSemaphore = Semaphore(0)

    abstract fun open()
    abstract fun getInputStream() : InputStream
    abstract fun getOutputStream() : OutputStream

    private var start = true

    override fun requestInputStream(): InputStream {
        if(start){
            output.interrupt()
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
            //println("reconnect")
            open()
            leaveSemaphore.release()
        }
        return getInputStream()
    }

    override fun requestOutputStream(): OutputStream {
        if(start){
            inputSemaphore.release()
            input.interrupt()
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
        port?.closePort()
        port = SerialPort.getCommPort(name).apply {
            baudRate = 115200
            setComPortTimeouts( TIMEOUT_READ_BLOCKING, 1000000000,1000000000)
            openPort()
        }
    }

    override fun getInputStream() : InputStream {
        return port!!.inputStreamWithSuppressedTimeoutExceptions
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


class BashConnection() : StreamConnection(){
    private var process: Process? = null
    override val connected: Boolean = process?.isAlive ?: false

    override fun open(){
        process?.destroy()
        val builder = ProcessBuilder()
        process = builder.command("/bin/bash").start()
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

fun main() {
    var line: String
    val scan = Scanner(System.`in`)

    val process = Runtime.getRuntime().exec("/bin/bash")
    val stdin = process.outputStream
    val stdout = process.inputStream

    val reader = BufferedReader(InputStreamReader(stdout))
    val writer = BufferedWriter(OutputStreamWriter(stdin))

    var input = scan.nextLine()
    input += "\n"
    writer.write(input)
    writer.flush()

    while (reader.readLine().also { line = it } != null) {
        println("Stdout: $line")
    }
}



