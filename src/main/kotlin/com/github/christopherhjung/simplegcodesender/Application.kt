package com.github.christopherhjung.simplegcodesender

import com.fazecast.jSerialComm.SerialPort
import java.io.BufferedReader
import java.io.IOException
import java.lang.Thread.interrupted
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.regex.Matcher
import java.util.regex.Pattern




class Command(val code: String, val arguments: String)
class CommandQueue(val items: LinkedBlockingQueue<Command> = LinkedBlockingQueue<Command>())

fun main(args: Array<String>) {
    /*val port = SerialPort.getCommPort("ttyAMA0")
    port.openPort()
    var controllerWorker : ControllerWorker? = null

    val queue = CommandQueue()
    val deviceWorker = DeviceWorker(port, queue){
        controllerWorker?.add(it)
    }
    deviceWorker.run()


    val server = ServerSocket(8080)
    while(!interrupted()){
        val socket = server.accept()

        controllerWorker = ControllerWorker(socket, queue)
        controllerWorker.run()
    }*/

    val leftConnector : Connector = StdInOutConnector()
    val rightConnector : Connector = StdOutConnector()

    val leftConnection = leftConnector.open()
    val rightConnection = rightConnector.open()

    val forwardWorker = Worker(leftConnection.inputStream, rightConnection.outputStream, listOf(GCodeFilter()))
    val backwardWorker = Worker(rightConnection.inputStream, leftConnection.outputStream, listOf())

    forwardWorker.start()
    backwardWorker.start()

    Thread.sleep(1000)
}
