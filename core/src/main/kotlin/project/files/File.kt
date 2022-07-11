package project.files

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import project.Project
import project.canon.CanonicalName
import java.nio.file.Path
import project.modules.Module
import kotlin.io.path.bufferedReader
import kotlin.io.path.createFile
import kotlin.io.path.notExists

/**
 * A [File] represents any file that can is a part of the entire project, being compiled by the compiler. This can be
 *  * Source files
 *  * Build files
 *  * Libraries
 *  * C headers
 *  * Caches
 *  * [TargetFile]s
 *  * Resources
 */
sealed class File(
    val canonName: CanonicalName,
    val path: Path,
    val project: Project
){
    abstract class ReadableFile<I>(
        canonName: CanonicalName,
        path: Path,
        project: Project
    ): File(canonName, path, project){
        abstract val input: Option<I>

        class SourceFile(
            canonName: CanonicalName,
            path: Path,
            project: Project
        ): ReadableFile<String>(canonName, path, project){
            override fun toData(): FileData.SourceFileData =
                FileData.SourceFileData(
                    canonName, path, input
                )

            override val input: Option<String>
                get() =
                    try{
                        path.bufferedReader().use { reader ->
                            reader.readText()
                        }.some()
                    }catch(e: Exception){
                        none()
                    }
        }
    }

    /**
     * A target file is a file that is the target for the output of the compiler. This varies based on the output data itself
     *
     * A target file can be for any output data whatsoever, including, but not limited to,
     *  * Binaries
     *  * Logs
     *  * Resources
     *  * Cache
     *  * Debug Symbols
     *  * IR libraries
     *
     * A target file must be grouped in a [Module.TargetModule], for clear association and identification of data.
     *
     * This is an abstract class because the test module needs to be able to extend this for rigid testing.
     * This will not be available for extension anywhere else, except for the api module, which should not be done at all.
     * The whole point of making this abstract is for testing only.
     */
    abstract class TargetFile(
        canonName: CanonicalName,
        path: Path,
        project: Project
    ): File(canonName, path, project){

        /**
         * This will dump the associated data to a file at the given path, relative to the project root
         *
         * For a target file, it will always be given a path relative to `root/target` unless otherwise specified
         * NOTE: Until more complex settings handling is implemented, `root/target` will be the hardcoded root of
         *  all target files
         *
         * This has to be abstract because not all target files are the same.
         *
         * @see Module.TargetModule
         */
        abstract fun dump()

        fun finish(){
            if(path.notExists()){
                path.createFile()
            }
            dump()
        }
    }

    abstract fun toData(): FileData
}