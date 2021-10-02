package com.github.christopherhjung.simplegcodesender

import java.text.SimpleDateFormat
import java.util.*


class TimeLogging() : Transformer{
    val part = TimeLoggingWorker()

    override fun createBackwardWorker(): List<Worker> {
        return listOf(part)
    }
}

class TimeLoggingWorker() : Worker(){
    private var dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSSX")

    override fun loop() {
        val line = adapter.take()
        val time = dateTimeFormatter.format(Date())
        adapter.offer("$time: $line")
    }
}



