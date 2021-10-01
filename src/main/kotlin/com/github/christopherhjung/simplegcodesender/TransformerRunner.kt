package com.github.christopherhjung.simplegcodesender

import kotlin.concurrent.thread

class TransformerRunner(val transformerGate: TransformerGate){
    private val thread = thread(false) {
        try{
            while(true){
                transformerGate.loop()
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
