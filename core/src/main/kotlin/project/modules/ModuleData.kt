package project.modules

import project.canon.CanonicalName
import project.files.FileData
import java.nio.file.Path

abstract class ModuleData(
    val canonName: CanonicalName,
    val path: Path,
    val children: List<ModuleData>,
    val files: List<FileData>
){
    class SourceModuleData(
        canonicalName: CanonicalName,
        path: Path,
        children: List<SourceModuleData>,
        files: List<FileData.SourceFileData>
    ): ModuleData(
        canonicalName,
        path, children, files
    )

    /**
     * This is an abstract class so that they can be extended outside the module when needed be, specifically in the test module
     *
     * This represents the immutable data for a [Module.TargetModule]
     */
    abstract class TargetModuleData(
        canonicalName: CanonicalName,
        path: Path,
        children: List<TargetModuleData>,
        files: List<FileData.TargetFileData>
    ): ModuleData(
        canonicalName,
        path,
        children,
        files
    )
}


