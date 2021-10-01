package com.github.christopherhjung.simplegcodesender

class NoEffect : TransformerGate(){
    override fun loop() {
        adapter.offer(adapter.take())
    }
}

abstract class TransformerGate{
    protected lateinit var adapter: Adapter
    abstract fun loop()
    fun setup(adapter: Adapter){
        this.adapter = adapter
    }
}

interface Transformer {
    fun forward() : TransformerGate{
        return NoEffect()
    }

    fun backward() : TransformerGate{
        return NoEffect()
    }
}

