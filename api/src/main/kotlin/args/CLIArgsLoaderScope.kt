package args

import arrow.core.none
import arrow.core.some
import display.DisplayManager
import driver.Driver

typealias ArgParser = (CLIArg)->Boolean

class CLIArgsLoaderScope(
    private val driver: Driver,
    private val cliArgsLoader: CLIArgsLoader,
    private val displayManager: DisplayManager
) {
    private var requirement: CLIRequirement? = null
    private val parsers = arrayListOf<ArgParser>()

    fun require(
        minLength: Int = 0,
        length: Int = minLength,
        maxLength: Int = minLength
    ){
        requirement = CLIRequirement(minLength, length, maxLength)
    }

    fun arg(parser: ArgParser){
        parsers.add(parser)
    }

    internal fun run(){
        CLIArgRunner(
            requirement,
            parsers,
            displayManager,
            cliArgsLoader,
            driver
        ).run()
    }
}

data class CLIRequirement(
    val minLength: Int,
    val length: Int,
    val maxLength: Int
)

class CLIArgRunner(
    val requirement: CLIRequirement?,
    val argParsers: List<ArgParser>,
    val displayManager: DisplayManager,
    val cliArgsLoader: CLIArgsLoader,
    val driver: Driver
){
    fun checkRequirements(){
        requirement?.apply {
            if(cliArgsLoader.args.size != length || cliArgsLoader.args.size < minLength || cliArgsLoader.args.size > maxLength){
                driver.errorOccurred = true
                displayManager.error(
                    cliArgsLoader.argsStr,
                    "Expected args length to be $length, or at least $minLength and at most $maxLength",
                    1,
                    0 until cliArgsLoader.argsStr.length,
                    "CLI"
                )
                return
            }
        }
    }

    fun runArgs(){
        while(cliArgsLoader.cursor.isDefined() && !driver.errorOccurred) {
            for(parser in argParsers){
                val result = cliArgsLoader.nextArg()
                val parserResult = result.map { it.map(parser) }
                if (parserResult.exists { it.isDefined() }) {
                    break
                }
                if (result.isLeft()) {
                    driver.errorOccurred = true
                    result.tapLeft {
                        val range = when (it) {
                            is ArgParserError.UnknownError -> 0 until cliArgsLoader.argsStr.length
                            is ArgParserError.CursorError -> {
                                val indexOf = cliArgsLoader.argsStr.indexOf(it.cursor.current)
                                indexOf until indexOf + it.cursor.current.length
                            }
                        }
                        displayManager.error(
                            cliArgsLoader.argsStr,
                            it.message,
                            1,
                            range,
                            "CLI"
                        )
                        return
                    }
                    result.tap {
                        cliArgsLoader.cursor.tap { cursor ->
                            val indexOf = cliArgsLoader.argsStr.indexOf(cursor.current)
                            val pos = indexOf until indexOf + cursor.current.length
                            displayManager.error(
                                cliArgsLoader.argsStr,
                                "Unrecognized argument",
                                1,
                                pos,
                                "CLI"
                            )
                        }
                    }
                    return
                }
            }
            cliArgsLoader.advance()
        }
    }

    fun run(){
        checkRequirements()
        runArgs()
    }
}