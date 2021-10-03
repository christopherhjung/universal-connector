package com.github.christopherhjung.simplegcodesender.transformer

import com.github.christopherhjung.simplegcodesender.Adapter
import java.util.concurrent.LinkedBlockingQueue

class NoEffect : Worker(){
    override fun loop() {
        adapter.offer(adapter.take())
    }
}

abstract class Worker{
    protected lateinit var adapter: Adapter
    abstract fun loop()
    fun setup(adapter: Adapter){
        this.adapter = adapter
    }
}

interface Transformer {
    fun connect(workers: List<Worker>, adapter: Adapter, block: (Worker) -> Unit): Adapter {
        var currentAdapter = adapter
        for(worker in workers){
            block(worker)
            worker.setup(currentAdapter)
            currentAdapter = Adapter(currentAdapter.output, LinkedBlockingQueue())
        }

        return currentAdapter
    }

    fun createForwardWorker() : List<Worker>{
        return listOf()
    }
    fun createBackwardWorker() : List<Worker>{
        return listOf()
    }

    fun forward(adapter: Adapter, block: (Worker) -> Unit ) : Adapter{
        return connect(createForwardWorker(), adapter, block)
    }
    fun backward(adapter: Adapter, block: (Worker) -> Unit ) : Adapter{
        return connect(createBackwardWorker(), adapter, block)
    }
}

