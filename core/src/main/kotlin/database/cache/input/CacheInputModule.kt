package database.cache.input

import project.Project
import project.canon.CanonicalName
import project.files.File
import project.modules.Module
import project.modules.ModuleData
import java.nio.file.Path

class CacheInputModule(
    canonicalName: CanonicalName,
    path: Path,
    project: Project
) : Module.ReadableModule(
    mutableListOf(),
    mutableListOf(),
    canonicalName,
    path,
    project
) {
    override fun loadFile(name: String, block: File.ReadableFile.SourceFile.() -> Unit) {

    }

    override fun toData(): CacheInputModuleData =
        CacheInputModuleData(
            canonicalName,
            path,
            children.filterIsInstance<CacheInputModule>().map { it.toData() },
            files.filterIsInstance<CacheInputFile>().map { it.toData() }
        )
}

class CacheInputModuleData(
    canonicalName: CanonicalName,
    path: Path,
    children: List<CacheInputModuleData>,
    files: List<CacheInputFileData>
): ModuleData(
    canonicalName,
    path,
    children,
    files
)