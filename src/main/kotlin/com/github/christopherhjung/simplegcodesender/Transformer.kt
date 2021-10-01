package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.LinkedBlockingQueue

class NoEffect : TransformerWorker(){
    override fun loop() {
        adapter.offer(adapter.take())
    }
}

abstract class TransformerWorker{
    protected lateinit var adapter: Adapter
    abstract fun loop()
    fun setup(adapter: Adapter){
        this.adapter = adapter
    }
}

interface Transformer {
    fun connect(workers: List<TransformerWorker>, adapter: Adapter, block: (TransformerWorker) -> Unit): Adapter {
        var currentAdapter = adapter
        for(worker in workers){
            block(worker)
            worker.setup(currentAdapter)
            currentAdapter = Adapter(currentAdapter.output, LinkedBlockingQueue())
        }

        return currentAdapter
    }

    fun createForwardWorker() : List<TransformerWorker>{
        return listOf()
    }
    fun createBackwardWorker() : List<TransformerWorker>{
        return listOf()
    }

    fun forward(adapter: Adapter, block: (TransformerWorker) -> Unit ) : Adapter{
        return connect(createForwardWorker(), adapter, block)
    }
    fun backward(adapter: Adapter, block: (TransformerWorker) -> Unit ) : Adapter{
        return connect(createBackwardWorker(), adapter, block)
    }
}

