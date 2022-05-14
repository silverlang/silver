package compiler

import arrow.core.getOrElse
import arrow.core.some
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import settings.CompilerSettings
import tokenizer.TokenPositionRange

/**
 * Here we have all the error codes that exist in the compiler. These will only be associated with error messages
 *
 * TODO: Add more codes
 */
//
enum class ErrorCode{
    BadChar,
}

//TODO: Add more codes
enum class InfoCode{}

//TODO: Add more codes
enum class WarningCode{}

sealed class CompilerMessageKind{
    class Info: CompilerMessageKind()
    class Warning: CompilerMessageKind()
    class Error(val code: ErrorCode): CompilerMessageKind()
}

data class CompilerMessage(
    val message: String,
    val kind: CompilerMessageKind,
    val pos: TokenPositionRange,
    val sourceFile: SourceFile
)

const val RED = "\\u001b[31m"
const val YELLOW = "\\u001b[33m"
const val BLUE = "\\u001b[34m"
const val RESET = "\\u001b[0m"

class CompilerMessageHandler{
    private val messages = ArrayDeque<CompilerMessage>()
    private var running = false

    fun pushMessage(message: String, kind: CompilerMessageKind, pos: TokenPositionRange, sourceFile: SourceFile){
        this.messages += CompilerMessage(
            message, kind, pos, sourceFile
        )
    }

    fun pushInfo(message: String, pos: TokenPositionRange, sourceFile: SourceFile){
        this.pushMessage(message, CompilerMessageKind.Info(), pos, sourceFile)
    }

    fun pushWarning(message: String, pos: TokenPositionRange, sourceFile: SourceFile){
        this.pushMessage(message, CompilerMessageKind.Warning(), pos, sourceFile)
    }

    fun pushError(message: String, code: ErrorCode, pos: TokenPositionRange, sourceFile: SourceFile){
        this.pushMessage(message, CompilerMessageKind.Error(code), pos, sourceFile)
    }

    private fun createMessageFromError(message: CompilerMessage): String {
        val pos = message.pos
        val source = message.sourceFile.getString(pos.some())
        val line = message.sourceFile.getLine(pos.startLine)
        val startStr = line.map { it.substring(0..pos.startCol) }
        val endStr = line.map { it.substring(pos.endCol) }
        val colorCode = when (
            message.kind
        ) {
            is CompilerMessageKind.Error -> RED
            is CompilerMessageKind.Info -> BLUE
            is CompilerMessageKind.Warning -> YELLOW
        }
        val kind = message.kind
        val prefix = when (kind){
            is CompilerMessageKind.Error -> "${RED}E${kind.code}${RESET}"
            is CompilerMessageKind.Info -> "${BLUE}I0${RESET}"
            is CompilerMessageKind.Warning -> "${YELLOW}W0${RESET}"
        }
        return line.map {
            """[${prefix}]: $message:
                |   ${startStr}${colorCode}${source}${RESET}${endStr}""".trimMargin()
        }.getOrElse { "" }
    }

    suspend fun processErrors(){
        running = true
        runBlocking {
            //TODO: Convert this launch to collect into a list of jobs so we can safely exit them all without [running]
            launch{
                while(running){
                    if(messages.isNotEmpty()){
                        val error = messages.removeFirst()
                        val message = createMessageFromError(error)
                        println(message)
                        delay(500)
                    }
                }

            }
        }
    }
}

class SilverCompiler(val settings: CompilerSettings) {
    val srcStorage = SourceStorage()
    val errorHandler = CompilerMessageHandler()
}