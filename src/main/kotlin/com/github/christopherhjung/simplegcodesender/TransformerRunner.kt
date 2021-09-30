package com.github.christopherhjung.simplegcodesender

import kotlin.concurrent.thread

class TransformerRunner(val transformerGate: TransformerGate){
    private val thread = thread(false) {
        while(true){
            transformerGate.loop()
        }
    }

    fun start(){
        thread.start()
    }
}
