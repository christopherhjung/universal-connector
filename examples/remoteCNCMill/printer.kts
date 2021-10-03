
config{
    input = Server(5555)
    output = Loopback()
    add(GCodeFilter())
}
