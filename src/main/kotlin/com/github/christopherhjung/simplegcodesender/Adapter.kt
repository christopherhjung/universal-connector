package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class Adapter(
    private val input: BlockingQueue<String>,
    private val output: BlockingQueue<String>
){
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