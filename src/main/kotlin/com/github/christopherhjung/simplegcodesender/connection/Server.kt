package com.github.christopherhjung.simplegcodesender.connection

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import javax.net.ServerSocketFactory

class Server(port: Int) : StreamConnection(){
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
