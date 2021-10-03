package com.github.christopherhjung.simplegcodesender

class ParserTest {
    fun test(){
        val result  = Executor.execute("4 + 5")
        println(result)

        config{
            input = StdInOut()
            output = Loopback()
            add(GCodeFilter())
        }
    }
}
