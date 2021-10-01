package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class OkBuffer() : Transformer{
    val sem = Semaphore(1)
    val abort = AtomicBoolean(false)
    val abortWithProgram = AtomicBoolean(false)
    val blocker = OkBlocker(sem, abort, abortWithProgram)
    val abortWorker = AbortWorker(abort, abortWithProgram)
    val opener = OkOpener(sem, abortWithProgram)

    override fun createForwardWorker(): List<Worker> {
        return listOf(abortWorker, blocker)
    }

    override fun createBackwardWorker(): List<Worker> {
        return listOf(opener)
    }
}

class AbortWorker(private val abort: AtomicBoolean, private val abortWithProgram: AtomicBoolean) : Worker(){
    override fun loop() {
        val line = adapter.take()
        if(line.contentEquals("!a", true)){
            abort.set(true)
        }else if(line.contentEquals("!ap", true)){
            abortWithProgram.set(true)
        }else{
            adapter.offer(line)
        }
    }
}

class OkBlocker(private val sem: Semaphore, val abort: AtomicBoolean, val abortWithProgram: AtomicBoolean) : Worker(){
    private val abortCode = listOf(
        "G90",
        "G0 Z10 F5000",
        "G0 Y0 F5000"
    )

    var lastProgramAbort = 0L

    override fun loop() {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        val withProgram = abortWithProgram.getAndSet(false)
        if(abort.getAndSet(false) || withProgram){
            adapter.clear()
            while(true){
                adapter.poll(1000) ?: break
            }

            if(withProgram){
                val currentAbort = System.currentTimeMillis()
                if(currentAbort - lastProgramAbort > 2000){
                    adapter.offerInput(abortCode)
                    lastProgramAbort = currentAbort
                }
            }
        }
        adapter.offer(adapter.take())
    }
}

class OkOpener(private val sem: Semaphore, private val abort: AtomicBoolean) : Worker(){
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

    override fun createBackwardWorker(): List<Worker> {
        return listOf(part)
    }
}

class OkFilterWorker() : Worker(){
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








