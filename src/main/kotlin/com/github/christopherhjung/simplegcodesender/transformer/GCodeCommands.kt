package com.github.christopherhjung.simplegcodesender.transformer

import com.github.christopherhjung.simplegcodesender.Adapter
import com.github.christopherhjung.simplegcodesender.ConfigScope

class GCodeCommands(val map: LinkedHashMap<Regex, Adapter.(MatchResult) -> Unit>) : Transformer {

    override fun createForwardWorker(): List<Worker> {
        return listOf(GCodeCommandsWorker(map))
    }
}

class GCodeCommandsWorker(val map: LinkedHashMap<Regex, Adapter.(MatchResult) -> Unit>) : Worker(){

    override fun loop() {
        var line = adapter.take().trim()

        for( (matcher, receiver) in map ){
            val result = matcher.matchEntire(line)
            if(result != null){
                receiver(adapter, result)
                return
            }
        }

        adapter.offer(line)
    }
}


class GCodeCommandsScope(){
    val map = LinkedHashMap<Regex, Adapter.(MatchResult) -> Unit>();
    fun command(matcher: Regex, block: Adapter.(MatchResult) -> Unit){
        map[matcher] = block
    }

    fun command(matcher: String, block: Adapter.(MatchResult) -> Unit){
        command(matcher.toRegex(), block)
    }
}


fun ConfigScope.commands(block: GCodeCommandsScope.() -> Unit){
    val scope = GCodeCommandsScope()
    scope.block()
    val command = GCodeCommands(scope.map)
    add(command)
}



