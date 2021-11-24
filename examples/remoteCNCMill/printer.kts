
config{
    input = Server(5555)
    output = Serial("/tmp/printer")
    add(GCodeFilter())
    add(GCodeControl())
}
//M92 Y-13.5
//M92 X-78

