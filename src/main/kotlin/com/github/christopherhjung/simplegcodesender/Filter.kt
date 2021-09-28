package com.github.christopherhjung.simplegcodesender

interface Filter {
    fun filter(input: String) : String
}

class GCodeFilter() : Filter{
    var lastCode: String = "G0"
    override fun filter(input: String): String {
        val line = input.trim().replace(" +".toRegex(), " ")

        return if(line.matches("[GM].+".toRegex(RegexOption.IGNORE_CASE))){
            val index = line.indexOf(" ")
            lastCode = line.substring(0, index)
            line
        }else{
            "$lastCode $line"
        }
    }
}
