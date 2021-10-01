package com.github.christopherhjung.simplegcodesender

import java.io.File
import java.io.FileNotFoundException


class FileLoader(dir: String = "./", pattern: String = "^!(.+)$" ) : Transformer{
    private val success = FileLoadSuccessWorker()
    private val part = FileLoaderWorker(File(dir.replace("\\ ", " ")), pattern.toRegex(), success)

    override fun createForwardWorker(): List<TransformerWorker> {
        return listOf(part)
    }

    override fun createBackwardWorker(): List<TransformerWorker> {
        return listOf(success)
    }
}

class FileLoadSuccessWorker() : TransformerWorker(){
    override fun loop() {
        adapter.offer(adapter.take())
    }

    fun send(msg: String){
        adapter.offer(msg)
    }
}

class FileLoaderWorker(val dir: File, val pattern: Regex, val success : FileLoadSuccessWorker) : TransformerWorker(){
    val map = mutableMapOf<Int, File>()
    override fun loop() {
        val line = adapter.take()

        val result = pattern.matchEntire(line)

        if(result != null){
            val fileName = result.groupValues[1].trim()
            if(fileName == "ls"){
                var i = 0
                map.clear()
                dir.walkTopDown().forEach {
                    if(it.isFile){
                        map[i] = it
                        val path = it.relativeTo(dir).path
                        success.send("($i) $path")
                        i++
                    }
                }
            }else{
                val number = fileName.toIntOrNull()

                val file = if(number != null){
                    map[number] ?: return adapter.offer("File $number not listed!")
                }else{
                    File(dir, fileName)
                }

                try{
                    val lines = file.readLines()
                    for(fileLine in lines){
                        adapter.offer(fileLine)
                    }
                    success.send("File $file loaded!")
                }catch (e: FileNotFoundException){
                    success.send("File $file not found!")
                }
            }
        }else{
            adapter.offer(line)
        }
    }
}

