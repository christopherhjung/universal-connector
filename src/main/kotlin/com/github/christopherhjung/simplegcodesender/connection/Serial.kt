package com.github.christopherhjung.simplegcodesender.connection

import com.fazecast.jSerialComm.SerialPort
import java.io.InputStream
import java.io.OutputStream

class Serial(val name: String) : StreamConnection(){
    private var port: SerialPort? = null
    override val connected: Boolean = port?.isOpen ?: false

    override fun open(){
        port?.closePort()
        port = SerialPort.getCommPort(name).apply {
            baudRate = 115200
            setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000000000,1000000000)
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
