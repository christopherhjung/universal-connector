package com.github.christopherhjung.simplegcodesender

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.system.exitProcess

fun main(args: Array<String>) = Hello().main(args)

class Hello : CliktCommand() {
    val first: String by option("--first", help="First connection").required()
    val second: String by option("--second", help="Second connection").required()
    val filters: List<String> by option("-f", help="Forward filters").multiple()

    val progresses = mutableListOf<TransformerRunner>()

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

    private fun createClassMap() : Map<String, KClass<*>>{
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
        map["PositionObserver"] = PositionObserver::class
        map["FileLoader"] = FileLoader::class
        map["BashConnection"] = BashConnection::class
        map["TimeLogging"] = TimeLogging::class
        return map
    }

    override fun run() {
        val map = createClassMap()

        val firstConnection = parse(first, map) as Connection
        val secondConnection = parse(second, map) as Connection


        val transformers = mutableListOf<Transformer>()

        for( filter in filters ){
            transformers.add(parse(filter, map) as Transformer)
        }

        connect(firstConnection.input.queue, secondConnection.output.queue, transformers)
        connect(secondConnection.input.queue, firstConnection.output.queue, transformers.reversed(), false)

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
            .group(2)

        val splitArguments = arguments.split(",")

        val objs = mutableListOf<Any>()

        if(arguments.isNotBlank()){
            for(argument in splitArguments){
                val argument = argument.trim()
                if(argument.matches("\\d+".toRegex())){
                    objs.add(argument.toInt())
                }else{
                    objs.add(argument)
                }
            }
        }

        val kclass = map[className]
        if(kclass != null){
            for(constructor in kclass.constructors){
                try{
                    return constructor.call(*objs.toTypedArray())
                }catch (ignore:Exception){
                    ignore.printStackTrace()
                }
            }
        }else{
            println("$className could not be found!")
            exitProcess(1)
        }
    }

    throw RuntimeException()
}
