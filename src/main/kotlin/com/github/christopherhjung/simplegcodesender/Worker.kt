package com.github.christopherhjung.simplegcodesender


class Worker(val input: Input, val output: Output, val filters: List<FilterPart>) : Thread() {
    override fun run() {
        try{
            while(!interrupted()){
                var line = input.read()

                for( filter in filters ){
                    line = filter.filter(line)
                }

                output.write(line)
            }
        }catch (ignore: EndInput){
            ignore.printStackTrace()
        }
    }
}
