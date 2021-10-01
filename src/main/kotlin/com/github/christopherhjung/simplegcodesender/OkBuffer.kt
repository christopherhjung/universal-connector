package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class OkBuffer() : Transformer{
    val sem = Semaphore(1)
    val error = AtomicBoolean(false)
    val blocker = OkBlocker(sem, error)
    val opener = OkOpener(sem, error)

    override fun forward(): TransformerGate {
        return blocker
    }

    override fun backward(): TransformerGate {
        return opener
    }
}

class OkBlocker(private val sem: Semaphore, val error: AtomicBoolean) : TransformerGate(){
    override fun loop() {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        if(error.getAndSet(false)){
            adapter.clear()
            while(true){
                adapter.poll(1000) ?: return
            }
        }
        adapter.offer(adapter.take())
    }
}

class OkOpener(private val sem: Semaphore, private val error: AtomicBoolean) : TransformerGate(){
    override fun loop() {
        val ok = adapter.take()
        if(ok == "ok"){
            sem.release()
        }else if(ok.startsWith("!!")){
            error.set(true)
            sem.release()
        }

        adapter.offer(ok)
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








