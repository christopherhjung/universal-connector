package com.github.christopherhjung.simplegcodesender

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.LinkedBlockingQueue

class GCodeFilterTest {

    fun process(inputContent: List<String>, speedMemory:Boolean = true) : List<String>{
        val gcodeWorker = GCodeWorker(speedMemory=speedMemory)

        val input = LinkedBlockingQueue<String>()
        val output =  LinkedBlockingQueue<String>()
        val adapter = Adapter(input,output)
        gcodeWorker.setup(adapter)


        for(cmd in inputContent){
            input.offer(cmd)
        }

        while(input.isNotEmpty()){
            gcodeWorker.loop()
        }

        return output.toList()
    }

    @Test
    fun checkAllGCodes(){
        val inputContent = listOf(
            "G0","G1","G2","G3","G4","G5","G6","G10","G11","G12","G17","G19","G20","G21","G26","G27","G28","G29","G29","G29","G29","G29","G29","G30","G31","G32","G33","G34","G35","G38.2","G38.3","G38.4","G38.5","G42","G53","G54","G55","G56","G57","G58","G59","G59.1","G59.2","G59.3","G60","G61","G76","G80","G90","G91","G92","G425","M0","M1","M3","M4","M5","M7","M8","M9","M10","M11","M16","M17","M18","M84","M20","M21","M22","M23","M24","M25","M26","M27","M28","M29","M30","M31","M32","M33","M34","M42","M43","M43 T","M48","M73","M75","M76","M77","M78","M80","M81","M82","M83","M85","M92","M100","M104","M105","M106","M107","M108","M109","M110","M111","M112","M113","M114","M115","M117","M118","M119","M120","M121","M122","M125","M126","M127","M128","M129","M140","M141","M143","M145","M149","M150","M154","M155","M163","M164","M165","M166","M190","M191","M192","M193","M200","M201","M203","M204","M205","M206","M207","M208","M209","M211","M217","M218","M220","M221","M226","M240","M250","M256","M260","M261","M280","M281","M282","M290","M300","M301","M302","M303","M304","M305","M350","M351","M355","M360","M361","M362","M363","M364","M380","M381","M400","M401","M402","M403","M404","M405","M406","M407","M410","M412","M413","M420","M421","M422","M425","M428","M430","M486","M500","M501","M502","M503","M504","M510","M511","M512","M524","M540","M569","M575","M600","M603","M605","M665","M665","M666","M666","M672","M701","M702","M710","M808","M810","M811","M812","M813","M814","M815","M816","M817","M818","M819","M851","M852","M860","M860","M869","M871","M876","M900","M906","M907","M908","M909","M910","M911","M912","M913","M914","M915","M916","M917","M918","M928","M951","M993","M994","M995","M997","M999","M7219","T0","T1","T2","T3","T4","T5","T6"
        )

        val expectedOutputContent = inputContent.filter { it != "G28" && it != "M84" }.map { GCode.withChecksum(it) }

        val actualOutputContent =  process(inputContent)

        assertEquals(expectedOutputContent, actualOutputContent)
    }

    @Test
    fun checkKlipperGCodes(){
        val inputContent = listOf(
            "FIRMWARE_RESTART",
            "RESTART"
        )

        val actualOutputContent =  process(inputContent)
        assertEquals(inputContent, actualOutputContent)
    }

    @Test
    fun customCommands(){
        val inputContent = listOf(
            "!A",
            "!AP"
        )

        val actualOutputContent =  process(inputContent)
        assertEquals(inputContent, actualOutputContent)
    }

    @Test
    fun outputUpperCase(){
        val inputContent = listOf(
            "!a",
            "!ap",
            "m114"
        )

        val expectedOutput = listOf(
            "!A",
            "!AP",
            GCode.withChecksum("M114")
        )

        val actualOutputContent =  process(inputContent)
        assertEquals(expectedOutput, actualOutputContent)
    }

    @Test
    fun rememberCommand(){
        val inputContent = listOf(
            "G0 X1",
            "X2",
            "Y1",
            "Y2",
            "Z1",
            "Z2",
            "G1 X1",
            "X2",
            "Y1",
            "Y2",
            "Z1",
            "Z2"
        )

        val expectedOutput = listOf(
            "G0 X1",
            "G0 X2",
            "G0 Y1",
            "G0 Y2",
            "G0 Z1",
            "G0 Z2",
            "G1 X1",
            "G1 X2",
            "G1 Y1",
            "G1 Y2",
            "G1 Z1",
            "G1 Z2"
        ).map { GCode.withChecksum(it) }

        val actualOutputContent =  process(inputContent, speedMemory=false)
        assertEquals(expectedOutput, actualOutputContent)
    }

    @Test
    fun rememberSpeed(){
        val inputContent = listOf(
            "G0 X1 F10000",
            "G1 X1 F200",
            "G0 Z1",
            "G1 Y1",
            "G2 Y1",
            "G3 Y2",
            "G2 X1 F300",
            "G0 X1",
            "G1 X1",
            "G2 X1",
            "G3 X1",
            "G3 X1 F400",
            "G0 X1",
            "G1 X1",
            "G2 X1",
            "G3 X1",
            "G0 X1 F1000",
            "G0 X1",
            "G1 X1",
            "G2 X1",
            "G3 X1"
        )

        val expectedOutput = listOf(
            "G0 X1 F10000",
            "G1 X1 F200",
            "G0 Z1 F10000",
            "G1 Y1 F200",
            "G2 Y1 F200",
            "G3 Y2 F200",
            "G2 X1 F300",
            "G0 X1 F10000",
            "G1 X1 F300",
            "G2 X1 F300",
            "G3 X1 F300",
            "G3 X1 F400",
            "G0 X1 F10000",
            "G1 X1 F400",
            "G2 X1 F400",
            "G3 X1 F400",
            "G0 X1 F1000",
            "G0 X1 F1000",
            "G1 X1 F400",
            "G2 X1 F400",
            "G3 X1 F400"
        ).map { GCode.withChecksum(it) }

        val actualOutputContent =  process(inputContent, speedMemory=true)
        assertEquals(expectedOutput, actualOutputContent)
    }
}
