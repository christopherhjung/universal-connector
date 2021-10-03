package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.transformer.Worker
import kotlin.concurrent.thread

class Runner(private val worker: Worker){
    private val thread = thread(false) {
        try{
            while(true){
                worker.loop()
            }
        }catch (ignore: InterruptedException){

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun start(){
        thread.start()
    }

    fun stop(){
        thread.interrupt()
    }
}
