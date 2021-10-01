package com.github.christopherhjung.simplegcodesender

import kotlin.concurrent.thread

class TransformerRunner(val transformerWorker: TransformerWorker){
    private val thread = thread(false) {
        try{
            while(true){
                transformerWorker.loop()
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
