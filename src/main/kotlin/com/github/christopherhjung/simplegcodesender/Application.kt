package com.github.christopherhjung.simplegcodesender

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.regex.Pattern
import kotlin.reflect.KClass


class Hello : CliktCommand() {
    val first: String by option("--first", help="First connection").required()
    val second: String by option("--second", help="Second connection").required()
    val filters: List<String> by option("-f", help="Forward filters").multiple()

    override fun run() {



        val firstConnection = parse(first) as Connection
        val secondConnection = parse(second) as Connection

        val forwardFilters = mutableListOf<FilterPart>()
        val backwardFilters = mutableListOf<FilterPart>()

        for( filter in filters ){
            val mappedFilter = parse(filter) as Filter
            forwardFilters.add(mappedFilter.forward())
            backwardFilters.add(mappedFilter.backward())
        }

        val forwardWorker = Worker(firstConnection.input, secondConnection.output, forwardFilters)
        val backwardWorker = Worker(secondConnection.input, firstConnection.output, backwardFilters.reversed())

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                firstConnection.close()
                secondConnection.close()

                return@runBlocking
            }
        })

        forwardWorker.start()
        backwardWorker.start()
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
