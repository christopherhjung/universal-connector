package com.github.christopherhjung.simplegcodesender

import java.io.File
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class OkBuffer() : Filter{
    val sem = Semaphore(1)
    val blocker = OkBlocker(sem)
    val opener = OkOpener(sem)


    override fun forward(): FilterPart {
        return blocker
    }

    override fun backward(): FilterPart {
        return opener
    }
}

class OkBlocker(val sem: Semaphore) : FilterPart{
    override fun filter(adapter: Adapter) {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        adapter.offer(adapter.take())
    }
}

class OkOpener(val sem: Semaphore) : FilterPart{
    var last = 0L
    override fun filter(adapter: Adapter) {
        val input = adapter.take()
        sem.release()
        adapter.offer(input)
    }
}


class OkFilter() : Filter{
    private val part = OkFilterPart()

    override fun forward(): FilterPart {
        return NoFilter
    }

    override fun backward(): FilterPart {
        return part
    }
}

class OkFilterPart() : FilterPart{
    var counter = 0
    var last = 0L
    override fun filter(adapter: Adapter) {
        val input = adapter.take()
        if(input == "ok"){
            counter++

            val current = System.currentTimeMillis()
            if( current - last > 1000 ){
                if(counter == 1){
                    adapter.offer("ok")
                }else{
                    adapter.offer("$counter*ok")
                }
                counter = 0
                last = current
            }
        }else{
            adapter.offer(input)
        }
    }
}



class PositionObserver(time: Number) : Filter{
    private val part = PositionObserverPart(time.toLong())

    override fun forward(): FilterPart {
        return part
    }

    override fun backward(): FilterPart {
        return NoFilter
    }
}

class PositionObserverPart(val delay: Long) : FilterPart{
    var last = System.currentTimeMillis()

    override fun filter(adapter: Adapter) {
        val line = adapter.poll(delay)
        if(line != null){
            adapter.offer(line)
        }
        val current = System.currentTimeMillis()
        if(current - last > delay){
            adapter.offer("M114")
            last = current
        }
    }
}




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

class FileLoadSuccessPart() : FilterPart{
    var last = System.currentTimeMillis()

    override fun filter(adapter: Adapter) {
        adapter.offer(adapter.take())
    }

    fun success(fileName: String){

    }
}

class FileLoaderPart(val dir: String, val success : FileLoadSuccessPart) : FilterPart{
    var last = System.currentTimeMillis()

    override fun filter(adapter: Adapter) {
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




