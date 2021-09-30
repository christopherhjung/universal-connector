package com.github.christopherhjung.simplegcodesender


class PositionObserver(time: Number) : Transformer{
    private val part = PositionObserverGate(time.toLong())

    override fun forward(): TransformerGate {
        return part
    }

    override fun backward(): TransformerGate {
        return NoEffect
    }
}

class PositionObserverGate(val delay: Long) : TransformerGate(){
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
