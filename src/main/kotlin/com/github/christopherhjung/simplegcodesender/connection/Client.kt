package com.github.christopherhjung.simplegcodesender.connection

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import javax.net.SocketFactory

class Client(val host: String, val port: Int) : StreamConnection(){
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
