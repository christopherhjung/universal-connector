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
    override fun filter(input: String): String {
        sem.tryAcquire(20000, TimeUnit.MILLISECONDS)
        return input
    }
}

class OkOpener(val sem: Semaphore) : FilterPart{
    override fun filter(input: String): String {
        if(input == "ok"){
            sem.release()
        }

        return input
    }
}

