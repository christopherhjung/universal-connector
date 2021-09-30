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

class OkBlocker(val sem: Semaphore) : FilterPart{
    override fun filter(input: String, callback : (String) -> Unit) {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        callback(input)
    }
}

class OkOpener(val sem: Semaphore) : FilterPart{
    var last = 0L
    override fun filter(input: String, callback : (String) -> Unit) {
        sem.release()
        callback(input)
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
    override fun filter(input: String, callback : (String) -> Unit) {
        if(input == "ok"){
            counter++

            val current = System.currentTimeMillis()
            if( current - last > 1000 ){
                if(counter == 1){
                    callback("ok")
                }else{
                    callback("$counter*ok")
                }
                counter = 0
                last = current
            }
        }else{
            callback(input)
        }
    }
}

