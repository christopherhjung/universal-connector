package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.connection.Serial
import com.github.christopherhjung.simplegcodesender.transformer.NoEffect
import com.github.christopherhjung.simplegcodesender.transformer.Transformer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class Starter {

    val progresses = mutableListOf<Runner>()

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
                val progress = Runner(it)
                progresses.add(progress)
            }
        }

        currentAdapter.output = output
        val closeLastAdapter = NoEffect()
        closeLastAdapter.setup(currentAdapter)
        val progress = Runner(closeLastAdapter)
        progresses.add(progress)
    }


    fun start(config: Config){
        val first = config.input
        val second = config.output
        val transformers = config.transformers

        val inputFeederFactory = LineFeederFactory()
        val outputFeederFactory = LineFeederFactory()

        val inputFeeder = inputFeederFactory.createFeeder(first)
        val outputFeeder = outputFeederFactory.createFeeder(second)

        connect(inputFeeder.input.queue, outputFeeder.output.queue, transformers)
        connect(outputFeeder.input.queue, inputFeeder.output.queue, transformers.reversed(), false)

        for(progress in progresses){
            progress.start()
        }

        inputFeeder.start()
        outputFeeder.start()

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                for(progress in progresses){
                    progress.stop()
                }

                inputFeeder.stop()
                outputFeeder.stop()
            }
        })

        println("Started!")
    }

}
