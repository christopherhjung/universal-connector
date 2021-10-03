package com.github.christopherhjung.simplegcodesender.transformer

class GCodeFilter() : Transformer {
    override fun createForwardWorker(): List<Worker> {
        return listOf(GCodeWorker())
    }
}

object GCode{
    fun checksum(cmd: String): Int {
        var checksum = 0
        for (data in cmd.toByteArray()) {
            checksum = checksum xor data.toInt()
        }
        return checksum
    }

    fun withChecksum(cmd: String) : String{
        return "$cmd*${checksum(cmd)}"
    }
}

class GCodeWorker(private val speedMemory : Boolean = true) : Worker(){
    private var lastCode: String = "G0"
    private var speedFinder = "F(\\d+)".toRegex()
    private var g0Speed = 10000
    private var g1Speed = 333

    private fun offerWithChecksum(cmd: String){
        var cmd = cmd
        val isG0 = cmd.startsWith("G0 ")
        if(speedMemory && ( isG0 || cmd.startsWith("G1 ") || cmd.startsWith("G2 ") || cmd.startsWith("G3 ") )){
            val result = speedFinder.find(cmd)
            if(result != null){
                val speed = result.groupValues[1].toInt()

                if(isG0){
                    g0Speed = speed
                }else{
                    g1Speed = speed
                }
            }else{
                cmd += " F" + if(isG0){
                    g0Speed
                }else{
                    g1Speed
                }
            }
        }else if(cmd.startsWith("G28") || cmd.startsWith("M84")){
            return
        }

        adapter.offer(GCode.withChecksum(cmd))
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

        if(line == "R"){
            return adapter.offer("FIRMWARE_RESTART")
        }

        if(line == "H"){
            return adapter.offer("G28")
        }

        if(line == "S"){
            return adapter.offer("M84")
        }

        if(line.matches("([^A-Z]|[A-Z_]{2}).*".toRegex())){
            return adapter.offer(line)
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

                if(part.matches("G[0123]".toRegex())){
                    lastCode = part
                }
            }else{
                if(currentCommand.isEmpty()){
                    if(line.matches("[XYZIJKF].*".toRegex())){
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



