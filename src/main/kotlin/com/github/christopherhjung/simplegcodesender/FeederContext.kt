package com.github.christopherhjung.simplegcodesender

class FeederContext<T> {
    lateinit var input: InputFeeder<T>
    lateinit var output: OutputFeeder<T>



    fun start(){
        input.start()
        output.start()
    }

    fun stop(){
        input.stop()
        output.stop()
    }
}
