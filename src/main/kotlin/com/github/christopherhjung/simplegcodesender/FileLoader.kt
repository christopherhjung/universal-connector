package com.github.christopherhjung.simplegcodesender

import java.io.File
import java.io.FileNotFoundException


class FileLoader(dir: String = "./", pattern: String = "^/(.+)$" ) : Transformer{
    private val notify = FileLoadSuccessWorker()
    private val part = FileLoaderWorker(File(dir.replace("\\ ", " ")), pattern.toRegex(), notify)

    override fun createForwardWorker(): List<Worker> {
        return listOf(part)
    }

    override fun createBackwardWorker(): List<Worker> {
        return listOf(notify)
    }
}

class FileLoadSuccessWorker() : Worker(){
    override fun loop() {
        adapter.offer(adapter.take())
    }

    fun send(msg: String){
        adapter.offer(msg)
    }
}

class FileLoaderWorker(val dir: File, val pattern: Regex, val notify : FileLoadSuccessWorker) : Worker(){
    var fileList = listOf<File>()
    override fun loop() {
        val line = adapter.take()
        val result = pattern.matchEntire(line)

        if(result != null){
            val fileName = result.groupValues[1].trim()
            if(fileName == "ls"){
                val fileList = mutableListOf<File>()
                dir.walkTopDown().forEach {
                    if(it.isFile){
                        fileList.add(it)
                    }
                }
                fileList.sortBy { it.path }
                for( (i, file) in fileList.withIndex() ){
                    val path = file.relativeTo(dir).path
                    notify.send("($i) $path")
                }
                this.fileList = fileList
            }else{
                val number = fileName.toIntOrNull()

                val file = if(number != null){
                    val fileList = fileList
                    if(number >= 0 && number < fileList.size){
                        fileList[number]
                    }else{
                        return notify.send("Index $number could not be load!")
                    }
                }else{
                    File(dir, fileName)
                }

                try{
                    val lines = file.readLines()
                    for(fileLine in lines){
                        adapter.offer(fileLine)
                    }
                    notify.send("File $file loaded!")
                }catch (e: FileNotFoundException){
                    notify.send("File $file not found!")
                }
            }
        }else{
            adapter.offer(line)
        }
    }
}

