package com.github.christopherhjung.simplegcodesender

import org.junit.jupiter.api.Test

class ParserTest {
    fun test(){
        val result  = Executor.execute("4 + 5")
        println(result)

        config{
            first = StdInOutConnection()
            second = Loopback()
            add(GCodeFilter())
        }
    }
}
