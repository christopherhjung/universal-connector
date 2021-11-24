package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.connection.Loopback
import com.github.christopherhjung.simplegcodesender.connection.StdInOut
import com.github.christopherhjung.simplegcodesender.transformer.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CommandsTest {
    @Test
    private fun testCommands() {
        config{
            input = StdInOut()
            //output = Client("192.168.178.59",5555)
            output = Loopback()
            fileLoader("/Users/chris/Fusion 360 CAM/nc/")
            timeLogging()
            okFilter()
            commands {
                command("s"){
                    offer("FIRMWARE_RESTART")
                }

                command("h"){
                    offer("G28")
                    offer("G92 X-78 Y-13.5")
                }

                command("s"){
                    offer("M84")


                }

            }
        }

        assertEquals(2, 2)
    }
}
