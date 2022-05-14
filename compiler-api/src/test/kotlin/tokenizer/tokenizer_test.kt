package tokenizer

import arrow.core.Either
import compiler.SilverCompiler
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import settings.CompilerSettings

fun main(args: Array<String>) = runBlocking{
    val settings = CompilerSettings.createSettingsFromArgs(args) ?: return@runBlocking //The called method will print the error for us
    val compiler = SilverCompiler(settings)
    val tokenizer = TokenizerBuilder.build(compiler){
        //Whitespace
        val ws = on(whitespace){
            val sk = skip
            println("Skipping whitespace")
            sk
        }
        //Newline
        val nl = char('\n') { skip then newline }
        //Underscore
        val underscore = pattern("_")
        val identStart = letter or (underscore then letter)
        val identBody = letter or digit
        val end = whitespace or punct
        //Ident tokens
        val ident = produce(ident) {
            val ident = identStart then (identBody until end)
            ident
        }
        //Int tokens
        val int = produce(int){
            val int = digit until end
            int
        }

        val all = (ident or int) then (ws or nl or eof)

        all until eof
    }
    launch{
        val tokens = when(val result = tokenizer.run()){
            is Either.Left -> {
                println(result.value.first)
                println(result.value.second)
                return@launch
            }
            is Either.Right -> result.value
        }
        for(token in tokens){
            println(tokens)
        }
    }
}