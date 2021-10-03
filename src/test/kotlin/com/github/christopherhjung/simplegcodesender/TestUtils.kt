package com.github.christopherhjung.simplegcodesender

import java.io.IOException
import java.net.ServerSocket

object TestUtils {
    fun findFreePort() : Int{
        for (port in 1000 until 2000) {
            try {
                ServerSocket(port).use { serverSocket ->
                    serverSocket.close()
                    return port
                }
            } catch (e: IOException) {

            }
        }

        throw RuntimeException("No Test port from 1000 to 2000 available")
    }
}
