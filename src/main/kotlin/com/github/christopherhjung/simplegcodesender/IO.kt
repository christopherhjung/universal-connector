package com.github.christopherhjung.simplegcodesender

import java.io.PrintWriter
import java.lang.RuntimeException


interface Input {
    fun read() : String
}

interface Output {
    fun write(line: String)
}


class EndInput : RuntimeException()


