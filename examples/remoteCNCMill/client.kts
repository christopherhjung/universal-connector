
config{
    input = StdInOut()
    output = Client("localhost",5555)
    add(FileLoader("/Users/chris/Fusion 360 CAM/nc/"))
    add(TimeLogging())
    add(OkFilter())
}
