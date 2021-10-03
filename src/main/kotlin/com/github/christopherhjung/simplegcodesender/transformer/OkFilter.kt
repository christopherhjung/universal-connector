package com.github.christopherhjung.simplegcodesender.transformer


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

