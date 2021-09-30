package com.github.christopherhjung.simplegcodesender

import java.io.File


class FileLoader(dir: String = "./", pattern: String = "^!(.+)$" ) : Filter{
    private val success = FileLoadSuccessPart()
    private val part = FileLoaderPart(dir, pattern.toRegex(), success)

    override fun forward(): FilterPart {
        return part
    }

    override fun backward(): FilterPart {
        return success
    }
}

class FileLoadSuccessPart() : FilterPart(){
    override fun loop() {
        adapter.offer(adapter.take())
    }

    fun success(fileName: String){
        adapter.offer("File $fileName loaded!")
    }
}

class FileLoaderPart(val dir: String, val pattern: Regex, val success : FileLoadSuccessPart) : FilterPart(){
    override fun loop() {
        val line = adapter.take()

        val result = pattern.matchEntire(line)

        if(result != null){
            val file = result.groupValues[1].trim()
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

