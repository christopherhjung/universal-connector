package com.github.christopherhjung.simplegcodesender

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPort.*
import java.io.*
import java.lang.RuntimeException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Semaphore

interface Connection{
    val connected: Boolean
    val input: Input
    val output: Output

    fun requestInputStream() : InputStream
    fun requestOutputStream() : OutputStream
}

class StdInOutConnection : Connection {
    override val input: Input = StreamInput(this)
    override val output: Output = StreamOutput(this)
    override val connected: Boolean = true

    override fun requestInputStream(): InputStream {
        return System.`in`
    }

    override fun requestOutputStream(): OutputStream {
        return System.out
    }
}

class StdOutConnection : Connection {
    override val input: Input = StreamInput(this)
    override val output: Output = StreamOutput(this)
    override val connected: Boolean = true

    override fun requestInputStream(): InputStream {
        return ByteArrayInputStream("".toByteArray())
    }

    override fun requestOutputStream(): OutputStream {
        return System.out
    }
}

abstract class StreamConnection : Connection{
    final override val input: Input = StreamInput(this)
    final override val output: Output = StreamOutput(this)

    private val inputSemaphore = Semaphore(1)
    private val outputSemaphore = Semaphore(1)

    abstract fun open()
    abstract fun getInputStream() : InputStream
    abstract fun getOutputStream() : OutputStream

    override fun requestInputStream(): InputStream {
        if(!connected){
            inputSemaphore.acquire()
            open()
            outputSemaphore.release()
        }
        return getInputStream()
    }

    override fun requestOutputStream(): OutputStream {
        if(!connected){
            inputSemaphore.release()
            outputSemaphore.acquire()
        }
        return getOutputStream()
    }
}

class ServerConnection(port: Int) : StreamConnection(){
    private val server = ServerSocket(port)
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
        return port!!.inputStream//inputStreamWithSuppressedTimeoutExceptions
    }

    override fun getOutputStream() : OutputStream {
        return port!!.outputStream
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
        socket = Socket(host, port)
    }

    override fun getInputStream() : InputStream {
        return socket!!.getInputStream()
    }

    override fun getOutputStream() : OutputStream {
        return socket!!.getOutputStream()
    }
}


