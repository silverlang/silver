package database.managers

import arrow.core.Option
import database.cache.input.CacheInputFile
import database.cache.input.CacheInputModule
import project.Project
import project.canon.toCanonicalName
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists

class CacheInputManager(
    private val dirPath: Path,
    private val filePath: Path,
    private val project: Project
) {
    fun getCacheInputModule(): CacheInputModule =
        dirPath.let {
            val dir = if(it.notExists()){
                it.createDirectories()
            }else{
                it
            }
            CacheInputModule(
                dir.toCanonicalName(),
                dir,
                project
            )
        }


    fun getCacheInputFileIfAbsent(): CacheInputFile =
        getCacheInputModule().let {
            val file = it.files.filterIsInstance<CacheInputFile>().find { file ->
                file.canonName == dirPath.toCanonicalName()
            }
            if(file == null){
                val filePath = if(filePath.notExists()){
                    filePath.createFile()
                }else{
                    filePath
                }
                val inputFile = CacheInputFile(
                    filePath.toCanonicalName(),
                    filePath,
                    project
                )
                it.files.add(inputFile)
                inputFile
            }else{
                file
            }
        }
}