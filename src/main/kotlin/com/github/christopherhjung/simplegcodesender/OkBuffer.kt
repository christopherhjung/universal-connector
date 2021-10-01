package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class OkBuffer() : Transformer{
    val sem = Semaphore(1)
    val abort = AtomicBoolean(false)
    val blocker = OkBlocker(sem, abort)
    val abortWorker = AbortWorker(abort)
    val opener = OkOpener(sem, abort)

    override fun createForwardWorker(): List<TransformerWorker> {
        return listOf(abortWorker, blocker)
    }

    override fun createBackwardWorker(): List<TransformerWorker> {
        return listOf(opener)
    }
}

class AbortWorker(private val abort: AtomicBoolean) : TransformerWorker(){
    override fun loop() {
        val line = adapter.take()
        if(line.contentEquals("a", true)){
            abort.set(true)
        }else{
            adapter.offer(line)
        }
    }
}

class OkBlocker(private val sem: Semaphore, val abort: AtomicBoolean) : TransformerWorker(){
    val abortCode = listOf(
        "G90",
        "G0 Z10 F5000",
        "G0 Y0 F5000"
    )

    override fun loop() {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        if(abort.getAndSet(false)){
            adapter.clear()
            while(true){
                adapter.poll(1000) ?: break
            }

            adapter.offerInput(abortCode)
        }
        adapter.offer(adapter.take())
    }
}

class OkOpener(private val sem: Semaphore, private val abort: AtomicBoolean) : TransformerWorker(){
    override fun loop() {
        val ok = adapter.take()
        if(ok == "ok"){
            sem.release()
        }else if(ok.startsWith("!!")){
            abort.set(true)
            sem.release()
        }

        adapter.offer(ok)
    }
}


class OkFilter() : Transformer{
    private val part = OkFilterWorker()

    override fun createBackwardWorker(): List<TransformerWorker> {
        return listOf(part)
    }
}

class OkFilterWorker() : TransformerWorker(){
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








