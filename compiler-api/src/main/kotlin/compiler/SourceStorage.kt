package compiler

import arrow.core.*
import database.DataBase
import tokenizer.TokenPosition
import tokenizer.TokenPositionRange
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.reader

data class SourceFile(
    val filePath: Path,
    val name: String,
    val source: String,
    val size: Int = source.length,
    val lines: List<String> = source.lines()
){

    fun hasLine(line: Int) = lines.size >= line
    fun getLine(line: Int): Option<String> = if(hasLine(line)) lines[line-1].some() else none()
    fun hasCol(line: Int, col: Int) = hasLine(line) && getLine(line).exists { it.length > col }
    fun getCol(line: Int, col: Int): Option<Char> = getLine(line).mapNotNull { it.getOrNull(col-1) }
    fun hasChar(char: Char) = source.contains(char)
    fun hasOffset(offset: Int) = source.length > offset

    operator fun get(pos: Option<TokenPosition>) = pos.flatMap { getCol(it.line, it.col) }
    fun getString(range: Option<TokenPositionRange>) = range.map { source.substring(it.start.pos..it.end.pos) }

    companion object{
        fun load(filePath: String): SourceFile{
            val file = Paths.get(filePath)
            val contents = file.reader().readText()
            return SourceFile(file, file.fileName.toString(), contents)
        }
    }
}

data class SourceFragment(
    val source: String,
    val pos: TokenPositionRange,
    val file: SourceFile
)

/**
 * This class is responsible for storing cached fragments of the source code per input file
 * This will make querying and searching for code simpler and faster
 */
class SourceStorage {
    private val fragments: DataBase<SourceFile, SourceFragment> = DataBase()

    fun pushSourceFragment(pos: TokenPositionRange, source: String, file: SourceFile){
        this.fragments.push(file, SourceFragment(source, pos, file))
    }

    fun allFragmentsFromFile(file: SourceFile): DataBase<SourceFile, SourceFragment> =
        this.fragments.transform { frag_file, frag_source ->
            if(file.filePath.absolutePathString() == frag_file.filePath.absolutePathString())
                frag_source
            else
                null
        }

    fun find(pos: TokenPositionRange, file: SourceFile): SourceFragment? =
        this.fragments.query { frag_file, frag_source ->
            frag_file.filePath.absolutePathString() == file.filePath.absolutePathString() && frag_source.pos == pos
        }
}