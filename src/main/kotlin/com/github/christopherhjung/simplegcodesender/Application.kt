package com.github.christopherhjung.simplegcodesender

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.christopherhjung.simplegcodesender.connection.*
import com.github.christopherhjung.simplegcodesender.transformer.*
import java.io.File
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.system.exitProcess

fun main(args: Array<String>) = Kts()/*.subcommands(Cli())*/.main(args)

class Kts : CliktCommand(name="kts") {
    val file: String by option("-f", help="File").required()

    override fun run() {
        val config = Config.fromFile(File(file))
        if(config == null){
            println("File could not be loaded")
            return
        }
        Starter.start(config)
    }
}

class Cli : CliktCommand(name="cli") {
    private val first: String by option("--first", help="First connection").required()
    private val second: String by option("--second", help="Second connection").required()
    private val filters: List<String> by option("--filter", help="Forward filters").multiple()

    private fun createClassMap() : Map<String, KClass<*>>{
        val map = mutableMapOf<String, KClass<*>>()
        map["ClientConnection"] = Client::class
        map["ServerConnection"] = Server::class
        map["StdInOutConnection"] = StdInOut::class
        map["StdOutConnection"] = StdOut::class
        map["SerialConnection"] = Serial::class
        map["GCodeFilter"] = GCodeFilter::class
        map["Loopback"] = Loopback::class
        map["OkBuffer"] = GCodeControl::class
        map["OkFilter"] = OkFilter::class
        map["PositionObserver"] = PositionObserver::class
        map["FileLoader"] = FileLoader::class
        map["BashConnection"] = Bash::class
        map["TimeLogging"] = TimeLogging::class
        return map
    }

    override fun run() {
        val result  = Executor.execute("import com.github.christopherhjung.simplegcodesender.*\nTimeLogging()")
        println(result)

        val map = createClassMap()

        val firstConnection = parse(first, map) as Connection
        val secondConnection = parse(second, map) as Connection

        val transformers = mutableListOf<Transformer>()

        for( filter in filters ){
            transformers.add(parse(filter, map) as Transformer)
        }

        val config = Config(firstConnection, secondConnection, transformers)
        Starter.start(config)
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
