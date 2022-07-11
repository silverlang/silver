package args

import arrow.core.*

data class ArgCursor(val current: String, val pos: Int, val rest: List<String>){
    fun next() =
        if(rest.isEmpty())
            none()
        else
            ArgCursor(rest[0], pos + 1, rest.slice(1 until rest.size)).some()
}

data class CLIArg(val name: String, val value: String)

sealed class ArgParserError(val message: String, val argPos: Int){
    class UnknownError(message: String, argPos: Int): ArgParserError(message, argPos)
    class CursorError(val cursor: ArgCursor, message: String, argPos: Int): ArgParserError(message, argPos)
}

class CLIArgsLoader(val args: Array<String>) {
    var cursor =
        if(args.isEmpty())
            none()
        else
            ArgCursor(args[0], 0, args.slice(1 until args.size)).some()

    val argsStr = args.joinToString(" ")

    private fun parse(): Either<ArgParserError, Option<CLIArg>>{
        cursor = cursor.map {
            if(!it.current.startsWith("-")){
                return ArgParserError.CursorError(it, "Expected '-' at the beginning of the argument", it.pos).left()
            }
            ArgCursor(it.current.substring(1 until it.current.length), it.pos, it.rest)
        }
        val split = cursor.map { it.current.split('=') }
        if(split.isEmpty()){
            return none<CLIArg>().right()
        }
        if(!split.exists { it.size == 2 }){
            return cursor.let {
                if (it is Some) {
                    ArgParserError.CursorError(it.value, "Arg must be a key and value separated by '='", it.value.pos).left()
                } else {
                    ArgParserError.UnknownError("Cannot display error, something went wrong.", 0).left()
                }
            }
        }
        return split.let {
            if(it is Some){
                CLIArg(it.value[0], it.value[1]).some().right()
            }else{
                ArgParserError.UnknownError("Cannot display error, something went wrong.", 0).left()
            }
        }
    }

    fun advance(){
        this.cursor = cursor.flatMap { it.next() }
    }

    fun nextArg(): Either<ArgParserError, Option<CLIArg>> =
        parse()

}