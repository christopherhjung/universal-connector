package com.github.christopherhjung.simplegcodesender.crap

import com.github.christopherhjung.simplegcodesender.Command
import com.github.christopherhjung.simplegcodesender.CommandQueue
import com.github.christopherhjung.simplegcodesender.Connection
import com.github.christopherhjung.simplegcodesender.Utils
import java.util.concurrent.LinkedBlockingQueue

class ControllerWorker(val connection: Connection, val outputQueue: CommandQueue) : Runnable{
    inner class InputWorker : Thread(){
        var lastCode: String = "G0"

        override fun run() {
            val reader = connection.inputStream.bufferedReader()

            while(!interrupted()){
                var line = Utils.interruptibleReadLine(reader)
                line = line.trim().replace(" +".toRegex(), " ")

                if(line.matches("[GM].+".toRegex())){
                    val index = line.indexOf(" ")
                    val code = line.substring(0, index)
                    val arguments = line.substring(index + 1)
                    outputQueue.items.add(Command(code, arguments))
                    lastCode = code
                }else{
                    outputQueue.items.add(Command(lastCode, line))
                }
            }
        }
    }

    inner class OutputWorker : Thread(){
        override fun run() {
            val element = inputQueue.take()
            val writer = connection.outputStream.bufferedWriter()
            writer.append(element)
            writer.newLine()
        }
    }

    val inputQueue = LinkedBlockingQueue<String>()

    fun add(str: String){
        inputQueue.offer(str)
    }

    val inputWorker = InputWorker()
    val outputWorker = OutputWorker()

    override fun run() {
        inputWorker.start()
        outputWorker.start()
    }
}
