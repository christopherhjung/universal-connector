package com.github.christopherhjung.simplegcodesender

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class TimeLogging() : Transformer{
    val part = TimeLoggingGate()
    override fun backward(): TransformerGate {
        return part
    }
}

class TimeLoggingGate() : TransformerGate(){
    private var dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSSX")

    private fun offerWithChecksum(cmd: String){
        if(!cmd.startsWith("G28")){
            adapter.offer("$cmd*${Checksum.xor(cmd)}")
        }
    }

    override fun loop() {
        val line = adapter.take()
        val time = dateTimeFormatter.format(Date())
        adapter.offer("$time: $line")
    }
}



