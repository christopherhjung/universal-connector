package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.connection.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter


class RuntimeTest {
    @Test
    fun loopback(){
        val inputStream = PipedInputStream()
        val writer = PrintWriter(PipedOutputStream(inputStream), true)

        val outputStream = PipedOutputStream()
        val reader = PipedInputStream(outputStream).bufferedReader()

        val first = config{
            input = StaticStreamConnection(inputStream, outputStream)
            output = Loopback()
        }

        Starter.start(first)

        for( i in 0 until 100 ){
            val random = RandomStringUtils.random(128)
            writer.println(random)
            val line = reader.readLine()
            assertEquals(random, line)
        }
    }

    @Test
    fun serverClient(){
        val inputStream = PipedInputStream()
        val writer = PrintWriter(PipedOutputStream(inputStream), true)

        val outputStream = PipedOutputStream()
        val reader = PipedInputStream(outputStream).bufferedReader()

        val port = TestUtils.findFreePort()

        val first = config{
            input = StaticStreamConnection(inputStream, outputStream)
            output = Server(port)
        }
        val second = config{
            input = Client("localhost", port)
            output = Loopback()
        }

        Starter.start(first)
        Starter.start(second)

        val random = RandomGCodeGenerator()

        for( i in 0 until 100 ){
            val cmd = random.nextGCode()
            writer.println(cmd)
            val line = reader.readLine()
            assertEquals(cmd, line)
        }
    }
}
