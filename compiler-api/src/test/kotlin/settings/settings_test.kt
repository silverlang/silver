import settings.CompilerSettings
import settings.CompilerSettingsBuilder
import settings.DebugOption
import java.nio.file.Paths

fun test1(){
    val settings = CompilerSettingsBuilder.build {
        this.inputFile{ Paths.get("test.ag") }
        this.debugOptions {
            it += DebugOption.TOKEN
        }
        true
    }
    println(settings)
}

fun test2(args: Array<String>){
    val settings = CompilerSettings.createSettingsFromArgs(args)
    println(settings)
}

fun main(args: Array<String>){
    test1()
    test2(args)
}