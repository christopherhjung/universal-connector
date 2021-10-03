
config{
    input = Server(5555)
    //output = Serial("/tmp/printer")
    output = StdInOut()
    add(GCodeFilter())
    add(GCodeControl())
}
