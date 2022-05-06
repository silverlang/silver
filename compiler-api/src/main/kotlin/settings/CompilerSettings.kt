package settings

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

const val COMPILER_VERSION = "0.0.1-alpha"

enum class DebugOption{
    TOKEN,
    AST,
    IR,
    BIN
}

const val DEFAULT_MAX_MEMORY = 1024*1024*1024
val CWD = Paths.get("").toAbsolutePath().toString()

data class CompilerSettings(
    val inputFile: Path,
    val maxMemory: Int,
    val debugOptions: ArrayList<DebugOption>,
    val outDir: Path,
    val dumpDir: Path,
){
    companion object{
        fun createSettingsFromArgs(args: Array<String>): CompilerSettings?{
            return CompilerSettingsBuilder.build {
                var failed = false
                this.inputFile {
                    val fullInput = CWD + '/' + args[0]
                    if(!Paths.get(fullInput).exists()){
                        println("File $fullInput does not exist! Aborting!")
                        failed = true
                        Paths.get("")
                    }else{
                        Paths.get(fullInput)
                    }
                }
                if(failed){
                    return@build false
                }
                args.slice(1 until args.size).forEach {
                    when{
                        it.startsWith("--") -> {
                            val splitResult = it.split("=")
                            when(splitResult[0]){
                                "--debug" -> {
                                    this.debugOptions { options ->
                                        when (splitResult[1]) {
                                            "token" -> {
                                                options.add(DebugOption.TOKEN)
                                            }
                                            "ast" -> {
                                                options.add(DebugOption.AST)
                                            }
                                            "ir" -> {
                                                options.add(DebugOption.IR)
                                            }
                                            "bin" -> {
                                                options.add(DebugOption.BIN)
                                            }
                                        }
                                    }
                                }
                                "--max_memory" -> {
                                    this.maxMemory {
                                        try{
                                            splitResult[1].toInt()
                                        }catch(e: Exception){
                                            println("WARNING: Invalid argument type for --max_memory: expected Int but got String: ${splitResult[1]}")
                                            DEFAULT_MAX_MEMORY
                                        }
                                    }
                                }
                                "--out" -> {
                                    this.outDir {
                                        val out = splitResult[1]
                                        val fullOut = CWD + out
                                        if(!Paths.get(fullOut).exists()){
                                            println("Expected dump dir $fullOut to exist, but it doesn't. Creating it now...")
                                            Paths.get(fullOut).createDirectories().toAbsolutePath()
                                        }else{
                                            Paths.get(fullOut).toAbsolutePath()
                                        }
                                    }
                                }
                                "--dump" -> {
                                    this.dumpDir {
                                        val dump = splitResult[1]
                                        val fullDump = CWD + dump
                                        if(!Paths.get(fullDump).exists()){
                                            println("Expected dump dir $fullDump to exist, but it doesn't. Creating it now...")
                                            Paths.get(fullDump).createDirectories().toAbsolutePath()
                                        }else{
                                            Paths.get(fullDump).toAbsolutePath()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                true
            }
        }
    }
}

class CompilerSettingsBuilder{
    var inputFile: Path = Paths.get("")
        private set
    var maxMemory: Int = DEFAULT_MAX_MEMORY
        private set
    val debugOptions: ArrayList<DebugOption> = arrayListOf()
    var outDir: Path = Paths.get("$CWD/out")
        private set
    var dumpDir: Path = Paths.get("$CWD/dump/")
        private set

    fun inputFile(callback: ()->Path): CompilerSettingsBuilder{
        this.inputFile = callback()
        return this
    }

    fun maxMemory(callback: ()->Int): CompilerSettingsBuilder{
        this.maxMemory = callback()
        return this
    }

    fun debugOptions(callback: (ArrayList<DebugOption>)->Unit): CompilerSettingsBuilder{
        callback(this.debugOptions)
        return this
    }

    fun outDir(callback: ()->Path): CompilerSettingsBuilder{
        this.outDir = callback()
        return this
    }

    fun dumpDir(callback: ()->Path): CompilerSettingsBuilder{
        this.dumpDir = callback()
        return this
    }

    companion object{
        fun build(callback: CompilerSettingsBuilder.()->Boolean): CompilerSettings?{
            val builder = CompilerSettingsBuilder()
            if(!builder.callback()){
                return null
            }
            return CompilerSettings(builder.inputFile, builder.maxMemory, builder.debugOptions, builder.outDir, builder.dumpDir)
        }
    }
}