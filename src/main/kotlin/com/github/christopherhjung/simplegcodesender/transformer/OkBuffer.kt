package com.github.christopherhjung.simplegcodesender.transformer

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class OkBuffer() : Transformer {
    val sem = Semaphore(1)
    val lock = AtomicBoolean(false)
    val blocker = OkBlocker(sem, lock)
    val notifier = Notifier()
    val abortWorker = AbortWorker(blocker, notifier, lock)
    val opener = OkOpener(sem, blocker)

    override fun createForwardWorker(): List<Worker> {
        return listOf(abortWorker, blocker)
    }

    override fun createBackwardWorker(): List<Worker> {
        return listOf(opener, notifier)
    }
}

class AbortWorker(private val blocker: OkBlocker, private val notifier:Notifier, private val lock :AtomicBoolean) : Worker(){
    override fun loop() {
        val line = adapter.take()
        if(line.contentEquals("!a", true)){
            lock.set(true)
            blocker.abort(false)
        }else if(line.contentEquals("!ap", true)){
            lock.set(true)
            blocker.abort(true)
        }else if(line.contentEquals("!u", true)){
            if(lock.getAndSet(false)){
                notifier.send("Unlocked!")
            }
        }else if(!lock.get()){
            adapter.offer(line)
        }
    }
}

class OkBlocker(private val sem: Semaphore, private val lock :AtomicBoolean) : Worker(){
    private val abortCode = listOf(
        "G90",
        "G0 Z10 F10000",
        "G0 Y0 F10000"
    )

    private var lastProgramAbort = 0L

    fun abort(withProgram: Boolean){
        lock.set(true)
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








