package project.modules

import project.Project
import project.canon.CanonicalName
import project.canon.toCanonicalName
import project.files.File
import project.files.FileData
import project.plus
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists
import java.nio.file.Path as JPath

sealed class Module(
    _files: List<File>,
    val children: List<Module>,
    val canonicalName: CanonicalName,
    val path: JPath,
    val project: Project
){
    abstract class ReadableModule(
        files: List<File>,
        children: List<ReadableModule>,
        canonicalName: CanonicalName,
        path: JPath,
        project: Project
    ): Module(files, children, canonicalName, path, project){
        class SourceModule(
            path: JPath,
            sourceFiles: List<File.ReadableFile.SourceFile>,
            modules: List<SourceModule>,
            project: Project
        ): ReadableModule(sourceFiles, modules, path.toCanonicalName(), path, project){
            override fun loadFile(name: String, block: File.ReadableFile.SourceFile.() -> Unit) {
                File.ReadableFile.SourceFile(
                    canonicalName + name,
                    path + "$name.ag",
                    project
                ).apply {
                    block()
                    files.add(this)
                }
            }

            override fun toData(): ModuleData.SourceModuleData =
                ModuleData.SourceModuleData(
                    canonicalName, path,
                    children.map { it as SourceModule }.map { it.toData() },
                    files.map { it as File.ReadableFile.SourceFile }.map { it.toData() }
                )

        }

        abstract fun loadFile(name: String, block: File.ReadableFile.SourceFile.()->Unit)
    }
    abstract class TargetModule(
        files: List<File.TargetFile>,
        children: List<TargetModule>,
        canonicalName: CanonicalName,
        path: JPath,
        project: Project
    ): Module(
        files, children, canonicalName, path, project
    ){
        abstract fun dump()

        protected abstract fun toTargetData(): ModuleData.TargetModuleData

        override fun toData(): ModuleData.TargetModuleData = toTargetData()

        fun finish(){
            if(path.notExists()){
                path.createDirectories()
            }
            files.map{
                it as File.TargetFile
            }.forEach {
                it.finish()
            }
        }
    }

    val files = _files.toMutableList()

    abstract fun toData(): ModuleData
}