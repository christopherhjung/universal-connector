package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.transformer.Worker
import kotlin.concurrent.thread

class TransformerRunner(val worker: Worker){
    private val thread = thread(false) {
        try{
            while(true){
                worker.loop()
            }
        }catch (e: InterruptedException){

        }
    }

    fun start(){
        thread.start()
    }

    fun stop(){
        thread.interrupt()
    }
}
