package com.github.christopherhjung.simplegcodesender

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class Starter {

    val progresses = mutableListOf<TransformerRunner>()

    companion object{
        fun start(config: Config){
            Starter().start(config)
        }
    }

    private fun connect(input: BlockingQueue<String>, output: BlockingQueue<String>, transformers: List<Transformer>, forward: Boolean = true){
        var currentAdapter = Adapter(input, LinkedBlockingQueue())

        for(transformer in transformers){
            val mapper = if(forward) {
                transformer::forward
            }else{
                transformer::backward
            }

            currentAdapter = mapper(currentAdapter){
                val progress = TransformerRunner(it)
                progresses.add(progress)
            }
        }

        currentAdapter.output = output
        val closeLastAdapter = NoEffect()
        closeLastAdapter.setup(currentAdapter)
        val progress = TransformerRunner(closeLastAdapter)
        progresses.add(progress)
    }


    fun start(config: Config){
        val first = config.input
        val second = config.output
        val transformers = config.transformers

        connect(first.input.queue, second.output.queue, transformers)
        connect(second.input.queue, first.output.queue, transformers.reversed(), false)

        for(progress in progresses){
            progress.start()
        }

        first.start()
        second.start()

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                for(progress in progresses){
                    progress.stop()
                }

                first.stop()
                second.stop()
            }
        })

        println("Started!")
    }

}
