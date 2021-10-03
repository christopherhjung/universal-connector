
config{
    input = Server(5555)
    output = Serial("/tmp/printer")
    add(GCodeFilter())
    add(OkBuffer())
}
