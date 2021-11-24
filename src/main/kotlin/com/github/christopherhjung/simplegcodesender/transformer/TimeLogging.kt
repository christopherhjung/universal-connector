package com.github.christopherhjung.simplegcodesender.transformer

import com.github.christopherhjung.simplegcodesender.ConfigScope
import java.text.SimpleDateFormat
import java.util.*

fun ConfigScope.timeLogging(){
    add(TimeLogging())
}

class TimeLogging() : Transformer {
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



