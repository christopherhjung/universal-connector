package com.github.christopherhjung.simplegcodesender

import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class GCodeFilter() : Filter{
    val part = GCodeFilterPart()
    override fun forward(): FilterPart {
        return part
    }
}

class Adapter(
    private val input: BlockingQueue<String>,
    private val output: BlockingQueue<String>){
    fun take() : String{
        return input.take()
    }

    fun offer(str: String){
        output.offer(str)
    }

    fun poll(millis: Long) : String?{
        return input.poll(millis, TimeUnit.MILLISECONDS)
    }
}

class FilterProgress(val adapter : Adapter, val filterPart: FilterPart){
    private val thread = thread {
        while(true){
            filterPart.filter(adapter)
        }
    }
}

class GCodeFilterPart() : FilterPart{
    var lastCode: String = "G0"
    override fun filter(adapter: Adapter) {
        val line = adapter.take().trim().replace(" +".toRegex(), " ")

        val command = if(!line.matches("[XYZIJKF].+".toRegex(RegexOption.IGNORE_CASE))){
            val index = line.indexOf(" ")
            lastCode = if(index != -1){
                line.substring(0, index)
            }else{
                line
            }
            line
        }else{
            "$lastCode $line"
        }

        if(command == "FIRMWARE_RESTART" || command == "r"){
            return adapter.offer("FIRMWARE_RESTART")
        }

        adapter.offer("$command*${Checksum.xor(command)}")
    }
}



