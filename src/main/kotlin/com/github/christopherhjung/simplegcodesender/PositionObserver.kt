package com.github.christopherhjung.simplegcodesender


class PositionObserver(time: Number) : Transformer{
    private val part = PositionObserverWorker(time.toLong())

    override fun createForwardWorker(): List<Worker> {
        return listOf(part)
    }
}

class PositionObserverWorker(val delay: Long) : Worker(){
    var last = System.currentTimeMillis()

    override fun loop() {
        val line = adapter.poll(delay)
        if(line != null){
            adapter.offer(line)
        }
        val current = System.currentTimeMillis()
        if(current - last > delay){
            adapter.offer("M114")
            last = current
        }
    }
}
