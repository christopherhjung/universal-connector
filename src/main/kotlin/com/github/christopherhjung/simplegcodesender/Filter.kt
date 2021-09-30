package com.github.christopherhjung.simplegcodesender

object NoFilter : FilterPart{
    override fun filter(adapter: Adapter) {
        adapter.offer(adapter.take())
    }
}

interface FilterPart{
    fun filter(adapter: Adapter)
}

interface Filter {
    fun forward() : FilterPart{
        return NoFilter
    }

    fun backward() : FilterPart{
        return NoFilter
    }
}

