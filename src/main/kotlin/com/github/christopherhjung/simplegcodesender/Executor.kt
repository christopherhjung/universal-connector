package com.github.christopherhjung.simplegcodesender

import javax.script.*

class Executor {
    companion object {
        var engine: ScriptEngine? = null

        fun execute(code: String): Any? {
            if (engine == null) {
                engine = ScriptEngineManager().getEngineByExtension("kts")
            }

            return engine?.eval(code)
        }
    }
}
