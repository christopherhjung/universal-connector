package com.github.christopherhjung.simplegcodesender

class GCodeFilter() : Filter{
    val part = GCodeFilterPart()
    override fun forward(): FilterPart {
        return part
    }
}

class GCodeFilterPart() : FilterPart{
    var lastCode: String = "G0"
    override fun filter(input: String): String {
        val line = input.trim().replace(" +".toRegex(), " ")

        val command = if(!line.matches("[XYZIJKF].+".toRegex(RegexOption.IGNORE_CASE))){
            val index = line.indexOf(" ")
            lastCode = if(index != -1){
                line.substring(0, index)
            }else{
                line
            }
            line
        }else{
            "$lastCode $line"
        }

        if(command == "FIRMWARE_RESTART" || command == "r"){
            return "FIRMWARE_RESTART"
        }

        return "$command*${Checksum.xor(command)}"
    }
}

