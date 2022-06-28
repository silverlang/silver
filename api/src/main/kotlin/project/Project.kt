package project

import arrow.core.*
import project.canon.CanonicalName
import project.files.File
import project.files.FileData
import project.modules.*
import java.nio.file.Path
import kotlin.io.path.Path
class ProjectScope {
    fun Project.findSourceFile(finder: (file: FileData) -> Boolean): Option<FileData.SourceFileData> =
        module {
            sourceModules.map { it.toData() }.map {
                it.findSourceFile(finder)
            }.map {
                it.getOrElse { null }
            }.find {
                it != null
            }.toOption()
        }


    fun Project.findSourceModule(finder: (module: ModuleData) -> Boolean): Option<ModuleData.SourceModuleData> =
        module {
            sourceModules.map { it.toData() }
                .find { finder(it) }
                .toOption() or
                    sourceModules.map { it.toData() }.map {
                        it.findSourceModule(finder)
                    }.map {
                        it.getOrElse { null }
                    }.find {
                        it != null
                    }.toOption()
        }

    fun Project.findTargetFile(finder: (FileData.TargetFileData) -> Boolean): Option<FileData.TargetFileData> =
        module {
            targetModules.map { it.toData() }.map {
                it.findTargetFile(finder)
            }.map {
                it.getOrElse { null }
            }.find {
                it != null
            }.toOption()
        }

    fun Project.findSourceFile(canonName: CanonicalName): Option<FileData.SourceFileData> =
        module {
            sourceModules.map { it.toData() }.map {
                it.findSourceFileByCanonName(canonName)
            }.map {
                it.getOrElse { null }
            }.find {
                it != null
            }.toOption()
        }

    fun Project.findSourceModule(canonName: CanonicalName): Option<ModuleData.SourceModuleData> =
        module {
            sourceModules.find { it.canonicalName == canonName }.toOption().map { it.toData() } or
                    sourceModules.map { it.toData() }.map {
                        it.findSourceModuleByCanonName(canonName)
                    }.map {
                        it.getOrElse { null }
                    }.find {
                        it != null
                    }.toOption()
        }

    fun Project.findTargetFile(canonName: CanonicalName): Option<FileData.TargetFileData> =
        module{
            targetModules.map { it.toData() }.map {
                it.findTargetFileByCanonName(canonName)
            }.map {
                it.getOrElse { null }
            }.find {
                it != null
            }.toOption()
        }


    fun Project.getSourceModulePath(path: Path) =
        this.srcRoot.resolve(path)

    fun Project.getSourceModulePath(path: String) = getSourceModulePath(Path(path))

    fun Project.makeSourceModule(
        path: String,
        then: Module.ReadableModule.SourceModule.() -> Unit
    ): Module.ReadableModule.SourceModule {
        val modulePath = this.getSourceModulePath(path)
        val module = Module.ReadableModule.SourceModule(
            modulePath,
            listOf(),
            listOf(),
            this
        )
        module.then()
        sourceModules += module
        return module
    }

    fun Project.makeSubproject(
        path: String,
        name: String,
        srcRoot: String = "src",
        then: Project.() -> Unit
    ): Project =
        Project(
            name,
            srcRoot,
            path,
            this.path + path
        ).apply(then).also {
            subprojects.add(it)
        }
}


inline fun <T> project(block: ProjectScope.()->T): T =
    ProjectScope().block()