package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
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
}

class FilterProgress(val adapter : Adapter, val filterPart: FilterPart){
    private val thread = thread {
        while(true){
            var str = adapter.take()
            str = filterPart.filter(str)
            adapter.offer(str)
        }
    }
}

class GCodeFilterPart() : FilterPart{
    var lastCode: String = "G0"
    override fun filter(input: String): String {
        val line = input.trim().replace(" +".toRegex(), " ")

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
            return "FIRMWARE_RESTART"
        }

        return "$command*${Checksum.xor(command)}"
    }
}

