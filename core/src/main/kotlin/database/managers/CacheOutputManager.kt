package database.managers

import arrow.core.Option
import database.cache.output.CacheOutputFile
import database.cache.output.CacheOutputModule
import database.data.DataElement
import project.Project
import project.canon.toCanonicalName
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists

class CacheOutputManager(
    private val cacheDirPath: Path,
    private val cacheFilePath: Path,
    private val project: Project,
) {
    fun getCacheOutputModule(): CacheOutputModule =
        cacheDirPath.let{
            val path = if(it.notExists()){
                it.createDirectories()
            }else{
                it
            }
            CacheOutputModule(
                path.toCanonicalName(),
                path,
                project
            )
        }

    fun getCacheOutputFileIfAbsent(rootSection: Option<DataElement.Section>) =
        getCacheOutputModule().let {
            val file = it.files.filterIsInstance<CacheOutputFile>().find {
                it.canonName == cacheFilePath.toCanonicalName()
            }
            if(file == null){
                val filePath = if(cacheFilePath.notExists()){
                    cacheFilePath.createFile()
                }else{
                    cacheFilePath
                }
                CacheOutputFile(
                    filePath.toCanonicalName(),
                    filePath,
                    project,
                    rootSection
                )
            }else{
                file
            }
        }
}