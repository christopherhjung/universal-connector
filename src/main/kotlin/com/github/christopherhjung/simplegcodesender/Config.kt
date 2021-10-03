package com.github.christopherhjung.simplegcodesender

import com.github.christopherhjung.simplegcodesender.connection.Connection
import com.github.christopherhjung.simplegcodesender.connection.Loopback
import com.github.christopherhjung.simplegcodesender.connection.StdInOut
import com.github.christopherhjung.simplegcodesender.transformer.GCodeFilter
import com.github.christopherhjung.simplegcodesender.transformer.Transformer
import org.apache.commons.codec.digest.DigestUtils
import java.io.*

class ConfigCache(){
    val cache: MutableMap<String, Config> = mutableMapOf()
}

class Config(val input: Connection, val output: Connection, val transformers: List<Transformer>){
    var hash: String = ""

    companion object{
        private val defaultImports = listOf(
            "import com.github.christopherhjung.simplegcodesender.*",
            "import com.github.christopherhjung.simplegcodesender.connection.*",
            "import com.github.christopherhjung.simplegcodesender.transformer.*",
        )
        fun fromFile(file: File) : Config?{
            val configStr = file.readText()
            val hash = DigestUtils.sha256Hex(configStr)

            /*val cacheFile = File("cache.config")
            val cache: ConfigCache = if(cacheFile.isFile){
                val inputStream = ObjectInputStream(FileInputStream(cacheFile))
                inputStream.readObject() as ConfigCache?
            } else {ConfigCache()} ?: ConfigCache()

            val cachedConfig = cache.cache[hash]
            if(cachedConfig != null){
                println("Load from cache!")
                return cachedConfig
            }*/

            try{
                val config = Executor.execute("${defaultImports.joinToString("\n")}\n$configStr") as Config?

                if(config != null){
                    config.hash = hash

                    /*cache.cache[hash] = config
                    val out = ObjectOutputStream(FileOutputStream("cache.config"))
                    out.writeObject(cache)
                    out.close()*/
                }

                return config
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

fun test(){
    config{
        input = StdInOut()
        output = Loopback()
        add(GCodeFilter())
    }
}
