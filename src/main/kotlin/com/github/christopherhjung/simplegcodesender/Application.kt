package com.github.christopherhjung.simplegcodesender

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.regex.Pattern
import kotlin.reflect.KClass

fun main(args: Array<String>) = Hello().main(args)

class Hello : CliktCommand() {
    val first: String by option("--first", help="First connection").required()
    val second: String by option("--second", help="Second connection").required()
    val filters: List<String> by option("-f", help="Forward filters").multiple()

    val progresses = mutableListOf<TransformerRunner>()

    private fun connect(input: BlockingQueue<String>, output: BlockingQueue<String>, filters: List<TransformerGate>){
        val queues = mutableListOf<BlockingQueue<String>>()
        queues.add(input)

        val filters = filters.toMutableList()

        if(filters.isEmpty()){
            filters.add(NoEffect())
        }else repeat(filters.size - 1){
            queues.add(LinkedBlockingQueue())
        }

        queues.add(output)

        for(element in queues.zipWithNext().zip(filters)){
            val (queue, filter) = element
            val adapter = Adapter(queue.first, queue.second)
            filter.setup(adapter)
            val progress = TransformerRunner(filter)
            progresses.add(progress)
        }
    }

    private fun createClassMap() : Map<String, KClass<*>>{
        val map = mutableMapOf<String, KClass<*>>()
        map["ClientConnection"] = ClientConnection::class
        map["ServerConnection"] = ServerConnection::class
        map["StdInOutConnection"] = StdInOutConnection::class
        map["StdOutConnection"] = StdOutConnection::class
        map["SerialConnection"] = SerialConnection::class
        map["GCodeFilter"] = GCodeTransformer::class
        map["Loopback"] = Loopback::class
        map["OkBuffer"] = OkBuffer::class
        map["OkFilter"] = OkFilter::class
        map["PositionObserver"] = PositionObserver::class
        map["FileLoader"] = FileLoader::class
        map["BashConnection"] = BashConnection::class
        map["NoFilter"] = NoFilter::class
        return map
    }

    override fun run() {
        val map = createClassMap()

        val firstConnection = parse(first, map) as Connection
        val secondConnection = parse(second, map) as Connection

        val forwardFilters = mutableListOf<TransformerGate>()
        val backwardFilters = mutableListOf<TransformerGate>()

        for( filter in filters ){
            val mappedFilter = parse(filter, map) as Transformer

            val forward = mappedFilter.forward()
            val backward = mappedFilter.backward()

            if(forward !is NoEffect){
                forwardFilters.add(forward)
            }

            if(backward !is NoEffect){
                backwardFilters.add(backward)
            }
        }

        connect(firstConnection.input.queue, secondConnection.output.queue, forwardFilters)
        connect(secondConnection.input.queue, firstConnection.output.queue, backwardFilters.reversed())

        for(progress in progresses){
            progress.start()
        }

        firstConnection.start()
        secondConnection.start()

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                for(progress in progresses){
                    progress.stop()
                }

                firstConnection.stop()
                secondConnection.stop()
            }
        })
    }
}

fun parse(str: String, map: Map<String, KClass<*>>) : Any{
    val pattern = Pattern.compile("^(\\w+)\\((.*)\\)$")
    val matcher = pattern.matcher(str.trim())

    if(matcher.find()){
        val className = matcher
            .group(1)

        val arguments = matcher
            .group(2).replace(" +".toRegex(), "")

        val splitArguments = arguments.split(",")

        val objs = mutableListOf<Any>()

        if(arguments.isNotBlank()){
            for(argument in splitArguments){
                if(argument.matches("\\d+".toRegex())){
                    objs.add(argument.toInt())
                }else{
                    objs.add(argument)
                }
            }
        }

        val kclass = map[className]
        for(constructor in kclass!!.constructors){
            try{
                return constructor.call(*objs.toTypedArray())
            }catch (ignore:Exception){
                ignore.printStackTrace()
            }
        }
    }

    throw RuntimeException()
}
