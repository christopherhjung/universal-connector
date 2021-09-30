package com.github.christopherhjung.simplegcodesender

import kotlin.concurrent.thread

class FilterProgress(val adapter : Adapter, val filterPart: FilterPart){
    private val thread = thread(false) {
        while(true){
            filterPart.loop()
        }
    }

    fun start(){
        thread.start()
    }
}
