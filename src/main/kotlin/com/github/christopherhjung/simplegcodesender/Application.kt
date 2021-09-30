package com.github.christopherhjung.simplegcodesender

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.regex.Pattern
import kotlin.reflect.KClass


class Hello : CliktCommand() {
    val first: String by option("--first", help="First connection").required()
    val second: String by option("--second", help="Second connection").required()
    val filters: List<String> by option("-f", help="Forward filters").multiple()

    fun connect(input: BlockingQueue<String>, output: BlockingQueue<String>, filters: List<FilterPart>){
        val queues = mutableListOf<BlockingQueue<String>>()
        queues.add(input)

        val filters = filters.toMutableList()


        if(filters.isEmpty()){
            filters.add(NoFilter)
        }else repeat(filters.size - 1){
            queues.add(LinkedBlockingQueue())
        }

        queues.add(output)

        val progresses = mutableListOf<FilterProgress>()

        for(element in queues.zipWithNext().zip(filters)){
            val (queue, filter) = element
            val adapter = Adapter(queue.first, queue.second)
            val progress = FilterProgress(adapter, filter)
            progresses.add(progress)
        }
    }

    override fun run() {
        val firstConnection = parse(first) as Connection
        val secondConnection = parse(second) as Connection

        val forwardFilters = mutableListOf<FilterPart>()
        val backwardFilters = mutableListOf<FilterPart>()

        for( filter in filters ){
            val mappedFilter = parse(filter) as Filter

            val forward = mappedFilter.forward()
            val backward = mappedFilter.backward()

            if(forward == NoFilter){
                forwardFilters.add(forward)
            }

            if(backward == NoFilter){
                backwardFilters.add(backward)
            }
        }

        connect(firstConnection.input.queue, secondConnection.output.queue, forwardFilters)
        connect(secondConnection.input.queue, firstConnection.output.queue, backwardFilters.reversed())

        firstConnection.start()
        secondConnection.start()

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                firstConnection.stop()
                secondConnection.stop()
            }
        })

    }
}

fun main(args: Array<String>) = Hello().main(args)


fun parse(str: String) : Any{

    val map = mutableMapOf<String, KClass<*>>()
    map["ClientConnection"] = ClientConnection::class
    map["ServerConnection"] = ServerConnection::class
    map["StdInOutConnection"] = StdInOutConnection::class
    map["StdOutConnection"] = StdOutConnection::class
    map["SerialConnection"] = SerialConnection::class
    map["GCodeFilter"] = GCodeFilter::class
    map["Loopback"] = Loopback::class
    map["OkBuffer"] = OkBuffer::class
    map["OkFilter"] = OkFilter::class

    val pattern = Pattern.compile("(\\w+)\\((.*?)\\)")
    val matcher = pattern.matcher(str)

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
        return kclass!!.constructors.first().call(*objs.toTypedArray())
    }

    throw RuntimeException()
}
