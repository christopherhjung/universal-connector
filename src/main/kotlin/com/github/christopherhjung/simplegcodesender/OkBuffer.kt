package com.github.christopherhjung.simplegcodesender

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

class OkBlocker(val sem: Semaphore) : FilterPart(){
    override fun loop() {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        adapter.offer(adapter.take())
    }
}

class OkOpener(val sem: Semaphore) : FilterPart(){
    var last = 0L
    override fun loop() {
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

class OkFilterPart() : FilterPart(){
    var counter = 0
    var last = 0L
    override fun loop() {
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








