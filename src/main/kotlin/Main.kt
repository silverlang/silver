fun main(args: Array<String>){
//    val settings = CompilerSettings.createSettingsFromArgs(args) ?: return@runBlocking //The called method will print the error for us
//    val compiler = SilverCompiler(settings)
//    val tokenizer = TokenizerBuilder.build(compiler){
//        //Whitespace
//        val ws = on(whitespace){
//            val sk = skip
//            sk
//        }
//        //Newline
//        val nl = char('\n') { skip then newline }
//        //terminator
//        val term = ws or nl or eof
//        //Underscore
//        val underscore = pattern("_")
//        val identStart = letter or (underscore then letter)
//        val identBody = letter or digit
//        val end = isPunct or term
//        //Ident tokens
//        val ident = produce(ident) {
//            val ident = identStart then (identBody until end)
//            ident
//        }
//        //Int tokens
//        val int = produce(int){
//            val int = digit until end
//            int
//        }
//
//        val punctTok = produce(punct){
//            isPunct
//        }
//
//        //All tokens
//        val all = (ident or int or punctTok)
//
//        (all or ws) until eof
//    }
//    launch{
//        val tokens = when(val result = tokenizer.run()){
//            is Either.Left -> {
//                println(result.value.first)
//                println(result.value.second)
//                return@launch
//            }
//            is Either.Right -> result.value
//        }
//    }
}