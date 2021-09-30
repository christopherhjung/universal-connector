package com.github.christopherhjung.simplegcodesender

import java.io.File
import java.io.FileNotFoundException


class FileLoader(dir: String = "./", pattern: String = "^!(.+)$" ) : Transformer{
    private val success = FileLoadSuccessGate()
    private val part = FileLoaderGate(dir, pattern.toRegex(), success)

    override fun forward(): TransformerGate {
        return part
    }

    override fun backward(): TransformerGate {
        return success
    }
}

class FileLoadSuccessGate() : TransformerGate(){
    override fun loop() {
        adapter.offer(adapter.take())
    }

    fun success(fileName: String){
        adapter.offer("File $fileName loaded!")
    }

    fun failure(fileName: String){
        adapter.offer("File $fileName not found!")
    }
}

class FileLoaderGate(val dir: String, val pattern: Regex, val success : FileLoadSuccessGate) : TransformerGate(){
    override fun loop() {
        val line = adapter.take()

        val result = pattern.matchEntire(line)

        if(result != null){
            val file = result.groupValues[1].trim()
            try{
                val lines = File(dir, file).readLines()
                for(fileLine in lines){
                    adapter.offer(fileLine)
                }
                success.success(file)
            }catch (e: FileNotFoundException){
                success.failure(file)
            }
        }else{
            adapter.offer(line)
        }
    }
}

