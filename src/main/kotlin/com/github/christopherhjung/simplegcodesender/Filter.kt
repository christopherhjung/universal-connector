package com.github.christopherhjung.simplegcodesender

object NoFilter : FilterPart(){
    override fun loop() {
        adapter.offer(adapter.take())
    }
}

abstract class FilterPart{
    protected lateinit var adapter: Adapter
    abstract fun loop()
    fun setup(adapter: Adapter){
        this.adapter = adapter
    }
}

interface Filter {
    fun forward() : FilterPart{
        return NoFilter
    }

    fun backward() : FilterPart{
        return NoFilter
    }
}

