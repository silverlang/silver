package display

class DisplayScope(private val displayManager: DisplayManager){
    inner class DisplayBuilder{
        var message: String = ""
        var source: String = ""
        var pos: IntRange = 0..0
        var line: Int = 0
        var file: String = ""
    }

    fun error(builder: DisplayBuilder.()->Unit){
        DisplayBuilder().let{
            it.builder()
            displayManager.error(it.source, it.message, it.line, it.pos, it.file)
        }
    }

    fun warning(builder: DisplayBuilder.()->Unit){
        DisplayBuilder().let{
            it.builder()
            displayManager.warning(it.source, it.message, it.line, it.pos, it.file)
        }
    }

    fun info(builder: DisplayBuilder.()->Unit){
        DisplayBuilder().let{
            it.builder()
            displayManager.info(it.source, it.message, it.line, it.pos, it.file)
        }
    }
}

