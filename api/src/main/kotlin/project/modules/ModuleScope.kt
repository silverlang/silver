package project.modules

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.or
import arrow.core.toOption
import project.canon.CanonicalName
import project.files.File
import project.files.FileData

class ModuleScope{
    val ModuleData.SourceModuleData.sourceFiles
        get() = files.map { it as FileData.SourceFileData }

    val ModuleData.SourceModuleData.sourceModules
        get() = children.map { it as ModuleData.SourceModuleData }

    val ModuleData.TargetModuleData.targetModules
        get() = children.map { it as ModuleData.TargetModuleData }

    val ModuleData.TargetModuleData.targetFiles
        get() = files.map { it as FileData.TargetFileData }

    fun ModuleData.SourceModuleData.findSourceFile(finder: (file: FileData)->Boolean): Option<FileData.SourceFileData> =
        sourceFiles.find{
            finder(it)
        }.toOption() or sourceModules.map {
                it.findSourceFile(finder).getOrElse { null }
            }.find {
                it != null
            }.toOption()

    fun ModuleData.SourceModuleData.findSourceModule(finder: (module: ModuleData)->Boolean): Option<ModuleData.SourceModuleData> =
        module {
            sourceModules.find {
                finder(it)
            }.toOption() or sourceModules.map {
                it.findSourceModule(finder).getOrElse { null }
            }.find {
                it != null
            }.toOption()
        }


    fun ModuleData.TargetModuleData.findTargetFile(finder: (file: FileData.TargetFileData)->Boolean): Option<FileData.TargetFileData> =
        targetFiles.find{
            finder(it)
        }.toOption() or children.map { it as ModuleData.TargetModuleData }.map {
            it.findTargetFile(finder).getOrElse { null }
        }.find {
            it != null
        }.toOption()

    fun ModuleData.TargetModuleData.findTargetModule(finder: (module: ModuleData.TargetModuleData)->Boolean): Option<ModuleData.TargetModuleData> =
        targetModules.find {
            finder(it)
        }.toOption() or targetModules.map {
            it.findTargetModule(finder).getOrElse { null }
        }.find {
            it != null
        }.toOption()

    fun ModuleData.SourceModuleData.findSourceFileByCanonName(canonicalName: CanonicalName): Option<FileData.SourceFileData> =
        findSourceFile {
            it.canonName == canonicalName
        }

    fun ModuleData.SourceModuleData.findSourceModuleByCanonName(canonicalName: CanonicalName): Option<ModuleData.SourceModuleData> =
        findSourceModule {
            it.canonName == canonicalName
        }

    fun ModuleData.TargetModuleData.findTargetFileByCanonName(canonicalName: CanonicalName): Option<FileData.TargetFileData> =
        findTargetFile{
            it.canonName == canonicalName
        }

    fun ModuleData.TargetModuleData.findTargetModuleByCanonName(canonicalName: CanonicalName): Option<ModuleData.TargetModuleData> =
        findTargetModule {
            it.canonName == canonicalName
        }
}

inline fun <T> module(block: ModuleScope.()->T): T =
    ModuleScope().block()
