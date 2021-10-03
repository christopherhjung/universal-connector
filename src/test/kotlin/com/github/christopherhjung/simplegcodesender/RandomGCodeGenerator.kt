package com.github.christopherhjung.simplegcodesender

import kotlin.random.Random

class RandomGCodeGenerator {
    private val random = Random(2222)

    fun Double.format(digits: Int) = "%.${digits}f".format(this)

    fun nextGCode() : String{
        var cmd = if(random.nextBoolean()) "G" else "M"
        cmd += random.nextInt(0, 999)

        if(random.nextBoolean()){
            cmd += "X" + random.nextDouble(-100.0, 100.0).format(2)
        }

        if(random.nextBoolean()){
            cmd += "Y" + random.nextDouble(-100.0, 100.0).format(2)
        }

        if(random.nextBoolean()){
            cmd += "Z" + random.nextDouble(-100.0, 100.0).format(2)
        }

        if(random.nextBoolean()){
            cmd += "F" + random.nextDouble(0.0, 10000.0).format(2)
        }

        return cmd
    }
}
