package tokenizer

import arrow.core.*
import compiler.SilverCompiler
import java.util.regex.Pattern

typealias ErrorMessage = Pair<String, TokenPositionRange>
typealias Predicate<T> = (T)->Boolean

sealed class ActionResult<T>(open val either: Either<ErrorMessage, Option<T>>){
    data class TokenizerResult(override val either: Either<ErrorMessage, Option<ActionValue.TokenizerValue>>): ActionResult<ActionValue.TokenizerValue>(either)
    data class TokenResult(override val either: Either<ErrorMessage, Option<ActionValue.TokenValue>>): ActionResult<ActionValue.TokenValue>(either)
}

sealed class ActionValue<T>(open val value: T){
    data class TokenizerValue(override val value: Tokenizer): ActionValue<Tokenizer>(value)
    data class TokenValue(override val value: Token): ActionValue<Token>(value)
}

sealed class TokenizerAction<T>{
    abstract fun run(tokenizer: Tokenizer): ActionResult<*>

    sealed class BaseAction(
        val action: TokenizerResultBuilder.(Tokenizer)->ActionResult.TokenizerResult
    ): TokenizerAction<Tokenizer>() {
        override fun run(tokenizer: Tokenizer): ActionResult.TokenizerResult =
            TokenizerResultBuilder.action(tokenizer)

        data class BasicAction(private val _action: TokenizerResultBuilder.(Tokenizer)->ActionResult.TokenizerResult): BaseAction(_action)

        sealed class TokenizerPredicatedAction<T>(
            open val predicate: Predicate<T>,
            open val then: (Tokenizer) -> Tokenizer,
            val handlePredicate: (Tokenizer, Predicate<T>)->Boolean,
            open val onFail: (Tokenizer, T?) -> String,
            val createT: (Tokenizer)->T?
        ): BaseAction(
            {
                if(handlePredicate(it, predicate)) {
                    success(then(it))
                }else {
                    val pos = when (it.currPos) {
                        is Some -> it.currPos.value..it.currPos.value
                        else    -> TokenPositionRange.default
                    }
                    val failData = createT(it)
                    failure(onFail(it, failData), pos)
                }
            }
        ){
            data class TokenizerCharPredicate(
                override val predicate: (Char)->Boolean,
                override val onFail: (Tokenizer, Char?) -> String
            ): TokenizerPredicatedAction<Char>(
                predicate,
                { tokenizer -> tokenizer.eatChar() },
                { tok, pred -> tok.currChar.exists(pred) },
                onFail,
                { it.currChar.getOrElse { null } }
            )

            data class TokenizerStringPredicate(
                override val predicate: (String)->Boolean,
                override val then: (Tokenizer)->Tokenizer,
                override val onFail: (Tokenizer, String?) -> String
            ): TokenizerPredicatedAction<String>(
                predicate,
                then,
                { tok, pred -> pred(tok.buffer) },
                onFail,
                { it.buffer }
            )
        }
    }


    data class TokenizerThen(val left: TokenizerAction<*>, val right: TokenizerAction<*>): TokenizerAction<Tokenizer>() {
        override fun run(tokenizer: Tokenizer): ActionResult.TokenizerResult =
            when(val lResult = left.run(tokenizer)){
                is ActionResult.TokenizerResult -> {
                    val newValue = when(val rValue = lResult.either) {
                        is Either.Left -> rValue
                        is Either.Right -> rValue.flatMap {
                            when (it) {
                                is Some ->
                                    when (val rResult = right.run(it.value.value)) {
                                        is ActionResult.TokenizerResult -> rResult.either
                                        else -> TokenizerResultBuilder.failure("", TokenPositionRange.default).either
                                    }
                                is None -> TokenizerResultBuilder.failure("", TokenPositionRange.default).either
                            }
                        }
                    }
                    ActionResult.TokenizerResult(newValue)
                }
                else -> TokenizerResultBuilder.failure("", TokenPositionRange.default)
            }
    }

    data class TokenizerOr(val left: TokenizerAction<*>, val right: TokenizerAction<*>): TokenizerAction<Tokenizer>() {
        override fun run(tokenizer: Tokenizer): ActionResult.TokenizerResult {
            return when(val lResult = left.run(tokenizer)){
                is ActionResult.TokenizerResult -> when(lResult.either) {
                    is Either.Left -> {
                        val rightRun = right.run(tokenizer) as ActionResult.TokenizerResult
                        rightRun
                    }
                    is Either.Right -> lResult
                }
                else -> TokenizerResultBuilder.eof()
            }
        }
    }

    class TokenizerOn(
        val predicate: (Char)->Boolean,
        val action: TokenizerAction<*>.()->TokenizerAction<*>
    ): TokenizerAction<Tokenizer>() {
        override fun run(tokenizer: Tokenizer): ActionResult.TokenizerResult {
            return if(tokenizer.currChar.exists(predicate)){
                action().run(tokenizer) as ActionResult.TokenizerResult
            }else{
                TokenizerResultBuilder.failure("Current char failed callback check", tokenizer.currPosRange.getOrElse { TokenPositionRange.default })
            }
        }
    }

    data class TokenProducer(
        val producer: (Tokenizer)->ActionResult.TokenResult
    ): TokenizerAction<Tokenizer>() {
        override fun run(tokenizer: Tokenizer): ActionResult.TokenResult {
            val token = when(val res = producer(tokenizer).either){
                is Either.Right ->
                    when(val op = res.value){
                        is Some -> op.value.value
                        is None -> {
                            val pos = when(val pos = tokenizer.currPosRange){
                                is Some -> pos.value
                                else -> return TokenResultBuilder.failure("No valid position found. This is a bug in the compiler!", TokenPositionRange.default)
                            }
                            return TokenResultBuilder.failure("Failed to produce token when one was expected", pos)
                        }
                    }
                is Either.Left -> return TokenResultBuilder.failure(res.value.first, res.value.second)
            }
            return TokenResultBuilder.success(token)
        }

    }

    data class TokenizerProducer(
        val producer: TokenProducer,
        val callback: ()->TokenizerAction<*>
    ): TokenizerAction<Tokenizer>() {
        override fun run(tokenizer: Tokenizer): ActionResult.TokenizerResult {
            val pos = when (tokenizer.currPos) {
                is Some -> tokenizer.currPos.value..tokenizer.currPos.value
                else    -> TokenPositionRange.default
            }
            val result = when(val result = callback().run(tokenizer)){
                is ActionResult.TokenizerResult -> when(val either = result.either){
                    is Either.Left -> return TokenizerResultBuilder.failure(either.value.first, either.value.second)
                    is Either.Right -> when(val rValue = either.value){
                        is Some -> rValue.value.value
                        is None -> {
                            return TokenizerResultBuilder.failure("Failed to get new tokenizer object", pos)
                        }
                    }
                }
                else -> return TokenizerResultBuilder.failure("Got wrong result object from callback: $result", pos)
            }
            val token = when(val pResult = producer.run(result).either){
                is Either.Left -> return TokenizerResultBuilder.failure(pResult.value.first, pResult.value.second)
                is Either.Right -> when(val rValue = pResult.value){
                    is Some -> rValue.value.value
                    else -> {
                        return TokenizerResultBuilder.failure("Failed to construct token", pos)
                    }
                }
            }
            val new = result.addToken(token)
            return TokenizerResultBuilder.success(new)
        }
    }

    data class TokenizerUntil(private val subject: TokenizerAction<*>, private val bound: TokenizerAction<*>): TokenizerAction<Tokenizer>(){
        override fun run(tokenizer: Tokenizer): ActionResult<*> {
            var tok = tokenizer
            var changed = false
            while(!this.bound.run(tok).either.exists { it.isDefined() }){
                when(val result = this.subject.run(tok)){
                    is ActionResult.TokenizerResult ->
                        when(val either = result.either){
                            is Either.Right ->
                                when(val op = either.value){
                                    is Some -> {
                                        tok = op.value.value
                                        changed = true
                                    }
                                    else -> break
                                }
                            is Either.Left -> return TokenizerResultBuilder.failure(either.value.first, either.value.second)
                        }
                    else -> return result
                }
            }
            return if(changed){
                TokenizerResultBuilder.success(tok)
            }else{
                TokenizerResultBuilder.failure("Nothing changed", tok.currPosRange.getOrElse { TokenPositionRange.default })
            }
        }

    }

    data class TokenizerPeek(
        val char: Char,
        val callback: () -> TokenizerAction<*>
    ): TokenizerAction<Tokenizer>(){
        override fun run(tokenizer: Tokenizer): ActionResult<*> {
            return if (tokenizer.peek { it == char }) {
                TokenizerResultBuilder.success(tokenizer)
            } else {
                val pos = when (val pos = tokenizer.currPosRange) {
                    is Some -> pos.value
                    is None -> return TokenizerResultBuilder.eof()
                }
                TokenizerResultBuilder.failure("Peek char does not match $char", pos)
            }
        }

    }

    infix fun then(next: TokenizerAction<Tokenizer>) = TokenizerThen(this, next)
    infix fun or(other: TokenizerAction<*>) = TokenizerOr(this, other)
    private fun charPred(char: Char) = BaseAction.TokenizerPredicatedAction.TokenizerCharPredicate(
        { it == char },
        { _, c -> "Current char $c does not match expected char $char" }
    )
    infix fun or(other: Char) = TokenizerOr(this, charPred(other))
    infix fun until(bound: TokenizerAction<*>) = TokenizerUntil(this, bound)

    interface ActionResultBuilder<T, R>{
        fun success(data: T): ActionResult<R>
        fun failure(message: String, pos: TokenPositionRange): ActionResult<R>
    }
    object TokenizerResultBuilder: ActionResultBuilder<Tokenizer, ActionValue.TokenizerValue> {
        override fun success(data: Tokenizer): ActionResult.TokenizerResult = ActionResult.TokenizerResult(ActionValue.TokenizerValue(data).some().right())
        override fun failure(message: String, pos: TokenPositionRange): ActionResult.TokenizerResult = ActionResult.TokenizerResult(ErrorMessage(message, pos).left())
        fun eof() = failure("EOF detected", TokenPositionRange.default)
    }
    object TokenResultBuilder: ActionResultBuilder<Token, ActionValue.TokenValue>{
        override fun success(data: Token): ActionResult.TokenResult = ActionResult.TokenResult(ActionValue.TokenValue(data).some().right())
        override fun failure(message: String, pos: TokenPositionRange): ActionResult.TokenResult = ActionResult.TokenResult(ErrorMessage(message, pos).left())
    }
}

class TokenizerBuilder private constructor() {
    val skip: TokenizerAction.BaseAction.BasicAction get() = TokenizerAction.BaseAction.BasicAction { ActionResult.TokenizerResult(it.next().right()) }
    val newline: TokenizerAction.BaseAction.BasicAction get() = TokenizerAction.BaseAction.BasicAction { ActionResult.TokenizerResult(it.nextLine().right()) }

    val digit = TokenizerAction.BaseAction.TokenizerPredicatedAction.TokenizerCharPredicate(
        { it.isDigit() },
        { _, ch -> "Current char is not a digit: $ch"}
    )

    val letter = TokenizerAction.BaseAction.TokenizerPredicatedAction.TokenizerCharPredicate(
        {
            it.isLetter()
        },
        { _, ch -> "Current char is not a letter: $ch" }
    )

    val whitespace: (Char)->Boolean = { it.isWhitespace() }

    val isPunct = TokenizerAction.BaseAction.TokenizerPredicatedAction.TokenizerCharPredicate(
        { Pattern.matches("\\p{Punct}", it.toString()) },
        { _, ch -> "Current char is not a punctuation: $ch" }
    )

    val punct =
        TokenizerAction.TokenProducer {
            val range = it.currPosRange.getOrElse { TokenPositionRange.default }
            if(Pattern.matches("\\p{Punct}", it.buffer)){
                TokenizerAction.TokenResultBuilder.success(Token(range, TokenValue.TokenChar(it.buffer[0])))
            }else{
                TokenizerAction.TokenResultBuilder.failure("Expected punct but did not find it", range)
            }
        }

    val int = TokenizerAction.TokenProducer {
        val range = when(val range = it.currPosRange){
            is Some -> range.value
            else -> TokenPositionRange.default
        }
        if(it.buffer.all { char -> Character.isDigit(char) }){
            TokenizerAction.TokenResultBuilder.success(Token(range, TokenValue.TokenInt(it.buffer.toInt())))
        }else{
            TokenizerAction.TokenResultBuilder.failure("Expected an integer but instead got ${it.buffer}", range)
        }
    }

    val ident = TokenizerAction.TokenProducer {
        val range = when(val range = it.currPosRange){
            is Some -> range.value
            else -> TokenPositionRange.default
        }
        TokenizerAction.TokenResultBuilder.success(Token(range, TokenValue.TokenIdent(it.buffer)))
    }

    val eof: TokenizerAction<*> get() = TokenizerAction.BaseAction.BasicAction {
        if(it.currChar.isEmpty())
            success(it)
        else {
            val range = when (val range = it.currPosRange) {
                is Some -> range.value
                else -> TokenPositionRange.default
            }
            failure("File has not ended yet", range)
        }
    }

    fun char(ch: Char, predicate: TokenizerAction<*>.()->TokenizerAction<*>) =
        TokenizerAction.BaseAction.TokenizerPredicatedAction.TokenizerCharPredicate(
            { it == ch },
            { _, c -> "Current char $c does not match expected char $ch" }
        ).predicate()

    fun pattern(pattern: String) =
        TokenizerAction.BaseAction.TokenizerPredicatedAction.TokenizerCharPredicate(
            { Pattern.matches(pattern, it.toString()) },
            { _, c -> "Current char $c does not match pattern $pattern" }
        )

    fun on(predicate: (Char)->Boolean, action: TokenizerAction<*>.()->TokenizerAction<*>) =
        TokenizerAction.TokenizerOn(predicate, action)

    fun produce(token: TokenizerAction.TokenProducer, producer: ()->TokenizerAction<*>) =
        TokenizerAction.TokenizerProducer(token, producer)

    fun peek(char: Char, callback: () -> TokenizerAction<*>) =
        TokenizerAction.TokenizerPeek(char, callback)

    companion object{
        fun build(compiler: SilverCompiler, callback: TokenizerBuilder.()->TokenizerAction<*>): TokenizerRunner{
            val builder = TokenizerBuilder()
            val chain = builder.callback()
            return TokenizerRunner(chain, Tokenizer(compiler))
        }
    }
}

data class TokenizerRunner(
    private val chain: TokenizerAction<*>,
    private val tokenizer: Tokenizer
){
    fun run(): Either<ErrorMessage, List<Token>> =
        when(val result = this.chain.run(this.tokenizer)){
            is ActionResult.TokenizerResult ->
                when(result.either){
                    is Either.Right ->
                        when(val value = result.either.value){
                            is Some -> value.value.value.tokens.right()
                            else -> ErrorMessage("Got None for tokenizer result value, this is a bug in the compiler.", TokenPositionRange.default).left()
                        }
                    is Either.Left -> result.either
                }
            else -> ErrorMessage("Got the wrong action result, expected Tokenizer result and not $result", TokenPositionRange.default).left()
        }
}
