package com.github.christopherhjung.simplegcodesender

object NoFilter : FilterPart{
    override fun filter(input: String, callback : (String) -> Unit) {
        callback(input)
    }
}

interface FilterPart{
    fun filter(input: String, callback : (String) -> Unit)
}

interface Filter {
    fun forward() : FilterPart{
        return NoFilter
    }

    fun backward() : FilterPart{
        return NoFilter
    }
}

