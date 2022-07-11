package display

import driver.Driver
import mu.KotlinLogging
import org.slf4j.LoggerFactory

enum class DisplayMode{
    ERROR, WARNING, INFO
}

class DisplayManager(private val driver: Driver) {
    fun display(mode: DisplayMode, source: String, message: String, line: Int, pos: IntRange, file: String){
        val color = when(mode){
            DisplayMode.ERROR -> ANSI_RED
            DisplayMode.WARNING -> ANSI_YELLOW
            DisplayMode.INFO -> ANSI_BLUE
        }
        if(source.isNotEmpty()){
            val start = source.substring(0 until pos.first)
            val offender = source.substring(pos)
            val end = source.substring(pos.last + 1 until source.length)
            println("$color[${mode.name}] $file@$line:${pos.first} - $line:${pos.last} - $message$ANSI_RESET")
            println("    $start$color$offender$ANSI_RESET$end")
        }else{
            println("$color[${mode.name}] $file@$line:${pos.first} - $line:${pos.last} - $message$ANSI_RESET")
        }
    }

    fun error(source: String, message: String, line: Int, pos: IntRange, file: String){
        display(DisplayMode.ERROR, source, message, line, pos, file)
    }

    fun warning(source: String, message: String, line: Int, pos: IntRange, file: String){
        display(DisplayMode.WARNING, source, message, line, pos, file)
    }

    fun info(source: String, message: String, line: Int, pos: IntRange, file: String){
        display(DisplayMode.INFO, source, message, line, pos, file)
    }

    companion object{
        const val ANSI_RED = "\u001B[91m"
        const val ANSI_YELLOW = "\u001B[93m"
        const val ANSI_BLUE = "\u001B[94m"
        const val ANSI_RESET = "\u001B[0m"
    }
}