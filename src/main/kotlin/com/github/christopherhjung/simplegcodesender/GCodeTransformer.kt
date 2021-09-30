package com.github.christopherhjung.simplegcodesender

class GCodeTransformer() : Transformer{
    val part = GCodeTransformerGate()
    override fun forward(): TransformerGate {
        return part
    }
}

class GCodeTransformerGate() : TransformerGate(){
    private var lastCode: String = "G0"
    override fun loop() {
        val line = adapter.take().trim().replace(" +".toRegex(), " ")

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
            return adapter.offer("FIRMWARE_RESTART")
        }

        adapter.offer("$command*${Checksum.xor(command)}")
    }
}



