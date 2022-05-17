package tokenizer

import arrow.core.*
import compiler.SilverCompiler
import compiler.SourceFile
import compiler.ErrorCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

data class TokenPosition(
    val line: Int,
    val col: Int,
    val pos: Int
){

    fun nextCol() = TokenPosition(this.line, this.col + 1, this.pos + 1)
    fun nextLine() = TokenPosition(this.line + 1, 1, this.pos + 1)

    operator fun rangeTo(other: TokenPosition) = TokenPositionRange(this, other)
    companion object{
        val default = TokenPosition(1, 1, 0)
    }
}

data class TokenPositionRange(
    val start: TokenPosition,
    val end: TokenPosition
){
    val startLine = start.line
    val endLine = end.line
    val lines = startLine .. endLine

    val startCol = start.col
    val endCol = end.col
    val cols = startCol .. endCol

    val startPos = start.pos
    val endPos = end.pos
    val poses = startPos .. endPos

    companion object{
        val default = TokenPositionRange(TokenPosition.default, TokenPosition.default)
    }
}

operator fun Option<TokenPosition>.rangeTo(other: Option<TokenPosition>): TokenPositionRange =
    zip(other).map { (start, end) -> start .. end }.getOrElse { TokenPositionRange.default }

sealed class TokenValue<T>(val value: T){
    data class TokenInt(val int: Int): TokenValue<Int>(int)
    data class TokenFloat(val float: Float): TokenValue<Float>(float)
    data class TokenIdent(val str: String): TokenValue<String>(str)
    data class TokenChar(val char: Char): TokenValue<Char>(char)
}

data class Token(
    val pos: TokenPositionRange,
    val value: TokenValue<*>
){
    fun toValue() = ActionValue.TokenValue(this)
}

class TokenStream(private val tokens: ArrayList<Token>): Iterable<Token>{
    override fun iterator(): Iterator<Token> {
        return TokenStreamIterator(tokens)
    }

    operator fun plusAssign(other: Token) = this.tokens.plusAssign(other)

}

class TokenStreamIterator(private val tokens: List<Token>): Iterator<Token>{
    private var index = 0

    override fun hasNext(): Boolean = this.index in 0..this.tokens.size

    override fun next(): Token = tokens[++index]

}

data class Tokenizer(
    internal val compiler: SilverCompiler,
    internal val currPos: Option<TokenPosition> = TokenPosition.default.some(),
    internal val currPosRange: Option<TokenPositionRange> = currPos.map { it .. it },
    internal val sourceFile: SourceFile = SourceFile.load(compiler.settings.inputFile.toAbsolutePath().toString()),
    internal val currChar: Option<Char> = sourceFile[currPos],
    internal val peekChar: Option<Char> = sourceFile[currPos.map { it.nextCol() }],
    internal val buffer: String = "",
    internal val tokens: List<Token> = arrayListOf()
){
    private val option: Option<ActionValue.TokenizerValue> = if(currChar.isDefined()) this.toValue().some() else none()

    init{
//        println("New tokenizer: $this")
    }
    fun next() =
        Tokenizer(
            this.compiler,
            this.currPos.map { it.nextCol() },
            tokens = this.tokens
        ).option

    fun nextLine() =
        Tokenizer(
            this.compiler,
            this.currPos.map { it.nextLine() },
            buffer = this.buffer
        ).option

    fun peek(callback: (Char)->Boolean): Boolean = this.peekChar.exists(callback)

    internal fun addToken(token: Token): Tokenizer =
        Tokenizer(
            compiler,
            currPos = token.pos.end.some(),
            currPosRange = token.pos.some(),
            tokens = tokens + token
        )

    fun eatChar() =
        Tokenizer(
            compiler,
            currPos.map { it.nextCol() },
            this.currPosRange.map { it.start..it.end.nextCol() },
            buffer = when(currChar){
                is Some -> buffer + currChar.value
                is None -> ""
            },
            tokens = tokens
        )

    fun toValue() = ActionValue.TokenizerValue(this)
}

//class Tokenizer(val compiler: SilverCompiler, filePath: String, override val coroutineContext: CoroutineContext): CoroutineScope {
//    private var currPos: Option<TokenPosition> = TokenPosition.default.some()
//    private val sourceFile = SourceFile.load(filePath)
//
//    private var currChar: Option<Char> = sourceFile[currPos]
//    private var peekChar: Option<Char> = sourceFile[nextPos()]
//
//    fun nextPos() = this.currPos.map { pos -> TokenPosition(pos.line, pos.col + 1, pos.pos + 1) }
//    fun nextLine() = this.currPos.map { pos -> TokenPosition(pos.line + 1, 1, pos.pos + 1) }
//
//    fun next(){
//        this.currPos = nextPos()
//        this.peekChar = sourceFile[this.nextPos()]
//    }
//
////    fun next(): Boolean{
////        this.currPos = nextPos()
////        /*
////         * Try to get the next char by doing a flat map on the current char
////         * This will be followed by a flatmap on the sourceFile.get with the newly acquired pos (see above)
////         * Which will decide whether we need to advance further until we reach a non-whitespace
////         */
////        this.peekChar = sourceFile[this.nextPos()]
////
//////        when(val peek = peekChar){
//////            is Some -> {
//////                if(peek.value.isWhitespace()){
//////                    while(this.currChar.exists { it.isWhitespace() }) {
//////                        this.currChar = this.currChar.flatMap chMap@{
//////                            sourceFile[currPos].flatMap {
//////                                when (it) {
//////                                    '\n', '\r' -> {
//////                                        this.currPos = nextLine()
//////                                        sourceFile[this.currPos]
//////                                    }
//////                                    else -> sourceFile[this.currPos]
//////                                }
//////                            }
//////                        }.orElse { sourceFile[currPos] }
//////                        this.peekChar = sourceFile[this.nextPos()]
//////                        println(this.currChar)
//////                    }
//////                }else{
//////                    this.currChar = this.peekChar
//////                    this.peekChar = sourceFile[this.nextPos()]
//////                }
//////            }
//////        }
////        return this.currChar.isDefined()
////    }
//
//    @ExperimentalCoroutinesApi
//    fun collect() = produce{
//        var lexeme = ""
//        var token: Token?
//        println("Starting token producer")
//        do{
//            val start = currPos
//            while(currChar.exists { !it.isWhitespace() })
//            when(val char = currChar){
//                is Some -> {
//                    println("Got char: $char")
//                    lexeme += char.value
//                    token = when{
//                        lexeme.all { it.isDigit() } ->
//                            Token(start..currPos, TokenValue.TokenInt(lexeme.toInt()))
//
//                        lexeme[0].isLetter() && lexeme.substring(1).all { it.isLetterOrDigit() } ->
//                            Token(start..currPos, TokenValue.TokenWord(lexeme))
//
//                        lexeme.length == 1 && Pattern.matches("\\p{Punct}", lexeme) ->
//                            Token(start..currPos, TokenValue.TokenChar(lexeme[0]))
//
//                        lexeme.contains(".")
//                                && lexeme.count { it == '.' } == 1
//                                && lexeme.split('.').all { str -> str.all { ch -> ch.isDigit() } } ->
//                            Token(start..currPos, TokenValue.TokenFloat(lexeme.toFloat()))
//
//                        else -> {
//                            compiler.errorHandler.pushError(
//                                "Unrecognized lexeme: $lexeme",
//                                ErrorCode.BadChar,
//                                start .. currPos,
//                                sourceFile
//                            )
//                            return@produce
//                        }
//                    }
////                    next()
//                }
//                else -> break
//            }
//            send(token!!)
//        }while(this@Tokenizer.next())
//    }
//}