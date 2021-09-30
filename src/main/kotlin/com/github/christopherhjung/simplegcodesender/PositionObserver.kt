package com.github.christopherhjung.simplegcodesender


class PositionObserver(time: Number) : Filter{
    private val part = PositionObserverPart(time.toLong())

    override fun forward(): FilterPart {
        return part
    }

    override fun backward(): FilterPart {
        return NoFilter
    }
}

class PositionObserverPart(val delay: Long) : FilterPart(){
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
