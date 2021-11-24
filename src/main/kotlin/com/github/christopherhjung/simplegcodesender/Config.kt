package com.github.christopherhjung.simplegcodesender


import com.github.christopherhjung.simplegcodesender.connection.Client
import com.github.christopherhjung.simplegcodesender.connection.Connection
import com.github.christopherhjung.simplegcodesender.connection.StdInOut
import com.github.christopherhjung.simplegcodesender.transformer.*
import java.io.File

class Config(val input: Connection, val output: Connection, val transformers: List<Transformer>){
    companion object{
        private val defaultImports = listOf(
            "import com.github.christopherhjung.simplegcodesender.*",
            "import com.github.christopherhjung.simplegcodesender.connection.*",
            "import com.github.christopherhjung.simplegcodesender.transformer.*",
        )
        fun fromFile(file: File) : Config?{
            val configStr = file.readText()

            try{
                return Executor.execute("${defaultImports.joinToString("\n")}\n$configStr") as Config?
            }catch (e: Exception){
                println(e.message)
            }

            return null
        }
    }
}
class ConfigScope{
    lateinit var input: Connection
    lateinit var output: Connection
    val transformers = mutableListOf<Transformer>()

    fun add(transformer: Transformer){
        transformers.add(transformer)
    }
}

fun config(block: ConfigScope.() -> Unit ) : Config{
    val scope = ConfigScope()
    scope.block()
    return Config(scope.input, scope.output, scope.transformers)
}
