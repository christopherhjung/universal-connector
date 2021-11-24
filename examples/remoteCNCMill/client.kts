import java.lang.Float.max
import kotlin.math.pow

config{
    val offset = 40.0f

    input = StdInOut()
    output = Client("192.168.178.59",5555)
    //output = Loopback()
    fileLoader("/Users/chris/Fusion 360 CAM/nc/")
    timeLogging()
    okFilter()
    commands {
        command("r"){
            offer("FIRMWARE_RESTART")
            offer("G28")
        }

        command("h"){
            offer("G28")
        }

        fun Adapter.enable(axis: String){
            val axis = axis.lowercase()
            offer("SET_STEPPER_ENABLE STEPPER=stepper_${axis} ENABLE=1")
            if(!axis.startsWith("z")){
                offer("SET_STEPPER_ENABLE STEPPER=stepper_${axis}1 ENABLE=1")
            }
        }

        command("g28.*".toRegex(RegexOption.IGNORE_CASE)){
            println("ignored home")
        }

        command("x"){
            enable("x")
            offer("G92 X-78")
        }

        command("y"){
            enable("y")
            offer("G92 Y-13.5")
        }

        command("i"){
            offer("G0 Z10 F10000")
            offer("G0 X0 Y0 F10000")
        }

        command("z".toRegex()){
            enable("z")
            offer("G92 Z0")
            offer("G0 Z10")
        }

        command("z\\s*(\\d+(?:\\.\\d+)?)".toRegex()){
            var height = it.groupValues[1].toFloat()
            enable("z")
            val position = height - offset
            offer("G92 Y" + -position)
            offer("G0 Y" + max(20.0f, 20.0f - position))
        }

        command("([xyz])\\s*(\\++)".toRegex()){
            var axis = it.groupValues[1].uppercase()
            var height = it.groupValues[2].length
            offer("G91")
            offer("G0 ${axis}0.${"0".repeat(height-1)}1")
            offer("G90")
        }

        command("([xyz])\\s*(\\-+)".toRegex()){
            var axis = it.groupValues[1].uppercase()
            var height = it.groupValues[2].length
            offer("G91")
            offer("G0 ${axis}-0.${"0".repeat(height-1)}1")
            offer("G90")
        }

        command("s"){
            offer("M84")
        }
    }
}
