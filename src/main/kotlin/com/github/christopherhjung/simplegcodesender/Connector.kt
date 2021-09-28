package com.github.christopherhjung.simplegcodesender

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

interface Connection{
    val inputStream: InputStream
    val outputStream: OutputStream
}

interface Connector {
    fun open() : Connection
}

class StdInOutConnection : Connection {
    override val inputStream: InputStream = System.`in`
    override val outputStream: OutputStream = System.out
}

class StdInOutConnector : Connector{
    override fun open(): Connection {
        return StdInOutConnection()
    }
}

class StdOutConnection : Connection {
    override val inputStream: InputStream = ByteArrayInputStream("".toByteArray())
    override val outputStream: OutputStream = System.out
}

class StdOutConnector : Connector{
    override fun open(): Connection {
        return StdOutConnection()
    }
}

class SocketConnector(socket: Socket) : Connection{
    val connected: Boolean = socket.isConnected
    override val inputStream: InputStream = socket.getInputStream()
    override val outputStream: OutputStream = socket.getOutputStream()
}

class ServerConnector(port: Int) : Connector{
    private val server = ServerSocket(port)
    override fun open(): Connection {
        return SocketConnector(server.accept())
    }
}

interface Source {
    fun read() : String
}

interface Target {
    fun write(line: String)
}




