package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class Adapter(
    var input: BlockingQueue<String>,
    var output: BlockingQueue<String>
){
    fun take() : String{
        return input.take()
    }

    fun clear(){
        input.clear()
    }

    fun clear(time : Long){
        while(true){
            poll(time) ?: return
        }
    }

    fun poll(millis: Long) : String?{
        return input.poll(millis, TimeUnit.MILLISECONDS)
    }

    fun offer(str: String){
        output.offer(str)
    }

    fun offer(lines: List<String>){
        for( line in lines ){
            offer(line)
        }
    }

    fun offerInput(str: String){
        input.offer(str)
    }

    fun offerInput(lines: List<String>){
        for( line in lines ){
            offerInput(line)
        }
    }
}
