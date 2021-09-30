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



class PositionObserver() : Filter{
    private val part = PositionObserverPart()

    override fun forward(): FilterPart {
        return part
    }

    override fun backward(): FilterPart {
        return NoFilter
    }
}

class PositionObserverPart() : FilterPart{
    var last = System.currentTimeMillis()
    override fun filter(adapter: Adapter) {
        adapter.poll(1000)
        val current = System.currentTimeMillis()
        if(current - last > 1000){
            adapter.offer("M114")
            last = current
        }
    }
}
