package com.github.christopherhjung.simplegcodesender

import java.io.File


class FileLoader(val dir: String = "") : Filter{
    private val success = FileLoadSuccessPart()
    private val part = FileLoaderPart(dir, success)

    override fun forward(): FilterPart {
        return part
    }

    override fun backward(): FilterPart {
        return NoFilter
    }
}

class FileLoadSuccessPart() : FilterPart(){
    var last = System.currentTimeMillis()

    override fun loop() {
        adapter.offer(adapter.take())
    }

    fun success(fileName: String){
        adapter.offer("File $fileName loaded!")
    }
}

class FileLoaderPart(val dir: String, val success : FileLoadSuccessPart) : FilterPart(){
    var last = System.currentTimeMillis()

    override fun loop() {
        val line = adapter.take()
        if(line.startsWith("!")){
            val file = line.drop(1).trim()
            val lines = File(dir, file).readLines()
            for(fileLine in lines){
                adapter.offer(fileLine)
            }
            success.success(file)
        }else{
            adapter.offer(line)
        }
    }
}

