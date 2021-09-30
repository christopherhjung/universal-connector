package com.github.christopherhjung.simplegcodesender

import kotlin.concurrent.thread

class FilterProgress(val adapter : Adapter, val filterPart: FilterPart){
    private val thread = thread {
        filterPart.setup(adapter)
        while(true){
            filterPart.loop()
        }
    }
}
