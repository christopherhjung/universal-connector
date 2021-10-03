package com.github.christopherhjung.simplegcodesender.transformer

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class OkBuffer() : Transformer {
    val sem = Semaphore(1)
    val blocker = OkBlocker(sem)
    val abortWorker = AbortWorker(blocker)
    val opener = OkOpener(sem, blocker)

    override fun createForwardWorker(): List<Worker> {
        return listOf(abortWorker, blocker)
    }

    override fun createBackwardWorker(): List<Worker> {
        return listOf(opener)
    }
}

class AbortWorker(private val blocker: OkBlocker) : Worker(){
    override fun loop() {
        val line = adapter.take()
        if(line.contentEquals("!a", true)){
            blocker.abort(false)
        }else if(line.contentEquals("!ap", true)){
            blocker.abort(true)
        }else{
            adapter.offer(line)
        }
    }
}

class OkBlocker(private val sem: Semaphore) : Worker(){
    private val abortCode = listOf(
        "G90",
        "G0 Z10 F10000",
        "G0 Y0 F10000"
    )

    private var lastProgramAbort = 0L

    fun abort(withProgram: Boolean){
        adapter.clear()

        if(withProgram){
            val currentAbort = System.currentTimeMillis()
            if(currentAbort - lastProgramAbort > 2000){
                adapter.offerInput(abortCode)
                lastProgramAbort = currentAbort
            }
        }
    }

    override fun loop() {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        adapter.offer(adapter.take())
    }
}

class OkOpener(private val sem: Semaphore, private val blocker: OkBlocker) : Worker(){
    override fun loop() {
        val ok = adapter.take()
        if(ok == "ok"){
            sem.release()
        }else if(ok.startsWith("!!")){
            blocker.abort(true)
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








