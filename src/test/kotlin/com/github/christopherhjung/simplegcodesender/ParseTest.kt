package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.connection.*
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter


class ParseTest {
    @Test
    fun parseExamples(){
        File("examples/simple").walkTopDown().forEach {
            if(it.isFile && it.extension == "kts"){
                val config = Config.fromFile(it)
                assertNotNull(config)
            }
        }
    }
}
