
config{
    input = StdInOut()
    output = Client("192.168.178.59",5555)
    add(FileLoader("/Users/chris/Fusion 360 CAM/nc/"))
    add(TimeLogging())
    add(OkFilter())
}
