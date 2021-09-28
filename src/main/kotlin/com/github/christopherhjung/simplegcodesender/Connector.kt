package com.github.christopherhjung.simplegcodesender

import java.io.*
import java.net.ServerSocket
import java.net.Socket

interface Connection{
    val connected: Boolean
    val input: Input
    val output: Output

    fun requestInputStream() : InputStream
    fun requestOutputStream() : OutputStream
}

interface Connector {
    fun open() : Connection
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

class StdInOutConnector : Connector{
    override fun open(): Connection {
        return StdInOutConnection()
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

class StdOutConnector : Connector{
    override fun open(): Connection {
        return StdOutConnection()
    }
}

abstract class StreamConnection : Connection{
    final override val input: Input = StreamInput(this)
    final override val output: Output = StreamOutput(this)
}

class SocketConnection(val connector: Connector, val socket: Socket) : StreamConnection(){
    override val connected: Boolean = socket.isConnected
    //override val input: Input = StreamInput(this)
    //override val output: Output = StreamOutput(this)

    fun checkConnection(){

    }

    override fun requestInputStream(): InputStream {
        return socket.getInputStream()
    }

    override fun requestOutputStream(): OutputStream {
        return socket.getOutputStream()
    }
}

class ServerConnector(port: Int) : Connector{
    private val server = ServerSocket(port)
    override fun open(): Connection {
        return SocketConnection(server.accept())
    }
}

interface Input {
    fun read() : String
}

interface Output {


    fun write(line: String)
}

class StreamInput(val connection: Connection) : Input {
    var reader = connection.requestInputStream().bufferedReader()

    override fun read(): String {
        if(!connection.connected){
            reader = connection.requestInputStream().bufferedReader()
        }

        return reader.readLine()
    }
}

class StreamOutput(val connection: Connection) : Output {
    var writer = connection.requestOutputStream().bufferedWriter()

    override fun write(line: String) {
        if(!connection.connected){
            writer = connection.requestOutputStream().bufferedWriter()
        }

        writer.write(line)
        writer.newLine()
    }
}




