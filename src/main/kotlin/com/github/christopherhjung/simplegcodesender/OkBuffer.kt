package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class OkBuffer() : Transformer{
    val sem = Semaphore(1)
    val blocker = OkBlocker(sem)
    val opener = OkOpener(sem)

    override fun forward(): TransformerGate {
        return blocker
    }

    override fun backward(): TransformerGate {
        return opener
    }
}

class OkBlocker(val sem: Semaphore) : TransformerGate(){
    override fun loop() {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        adapter.offer(adapter.take())
    }
}

class OkOpener(val sem: Semaphore) : TransformerGate(){
    override fun loop() {
        sem.release()
        adapter.offer(adapter.take())
    }
}

class NoFilter() : Transformer{
    override fun backward(): TransformerGate {
        return NoEffect()
    }
}

class OkFilter() : Transformer{
    private val part = OkFilterGate()

    override fun backward(): TransformerGate {
        return part
    }
}

class OkFilterGate() : TransformerGate(){
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








