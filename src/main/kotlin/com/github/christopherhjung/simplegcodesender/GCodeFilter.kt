package com.github.christopherhjung.simplegcodesender

class GCodeFilter() : Transformer{
    val part = GCodeTransformerGate()
    override fun forward(): TransformerGate {
        return part
    }
}

class GCodeTransformerGate() : TransformerGate(){
    private var lastCode: String = "G0"

    private fun offerWithChecksum(cmd: String){
        if(!cmd.startsWith("G28") && !cmd.startsWith("M84")){
            adapter.offer("$cmd*${Checksum.xor(cmd)}")
        }
    }

    override fun loop() {
        var line = adapter.take().trim()

        if(line.isEmpty()){
            return
        }

        if(line.startsWith("(")){
            return
        }

        line = line.uppercase()

        if(line == "FIRMWARE_RESTART" || line == "R"){
            return adapter.offer("FIRMWARE_RESTART")
        }

        if(line == "H"){
            return adapter.offer("G28")
        }

        if(line == "S"){
            return adapter.offer("M84")
        }

        val parts = line.split(" +".toRegex())
        var currentCommand = ""
        for( (i, part) in parts.withIndex() ){
            val isLast = i == parts.size - 1

            if( part.startsWith("G") ){
                if(currentCommand.isNotEmpty()){
                    offerWithChecksum(currentCommand)
                }
                currentCommand = part
                lastCode = part
            }else{
                if(currentCommand.isEmpty()){
                    if(line.matches("[XYZIJKF].+".toRegex())){
                        currentCommand = lastCode
                    }
                }

                if(currentCommand.isNotEmpty()){
                    currentCommand += " "
                }

                currentCommand += part
            }


            if(isLast){
                offerWithChecksum(currentCommand)
            }
        }
    }
}



