package com.github.christopherhjung.simplegcodesender

object NoFilter : FilterPart{
    override fun filter(input: String): String {
        return input
    }
}

interface FilterPart{
    fun filter(input: String) : String
}

interface Filter {
    fun forward() : FilterPart{
        return NoFilter
    }

    fun backward() : FilterPart{
        return NoFilter
    }
}

