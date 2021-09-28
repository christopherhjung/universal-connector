package com.github.christopherhjung.simplegcodesender.crap

import com.fazecast.jSerialComm.SerialPort
import com.github.christopherhjung.simplegcodesender.CommandQueue
import com.github.christopherhjung.simplegcodesender.Utils

class DeviceWorker(val socket: SerialPort, val queue: CommandQueue, val output : (String) -> Unit) : Runnable{
    inner class InputWorker : Thread(){

        override fun run() {
            val reader = socket.inputStream.bufferedReader()

            while(!interrupted()){
                val line = Utils.interruptibleReadLine(reader)
                output(line)
            }
        }
    }

    inner class OutputWorker : Thread(){
        override fun run() {
            val command = queue.items.take()
            val writer = socket.outputStream.bufferedWriter()
            writer.append(command.code)
            writer.append(" ")
            writer.append(command.arguments)
            writer.newLine()
        }
    }

    val inputWorker = InputWorker()
    val outputWorker = OutputWorker()

    override fun run() {
        inputWorker.start()
        outputWorker.start()
    }
}
