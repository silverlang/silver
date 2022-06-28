package project

import project.files.File
import project.modules.Module
import java.nio.file.Path as JPath
import kotlin.io.path.Path

class Project(
    val projectName: String,
    srcRoot: String,
    pathStr: String,
    val path: JPath = Path(pathStr)
) {
    val srcRoot: JPath = path.resolve(Path(srcRoot))
    val sourceModules = arrayListOf<Module.ReadableModule.SourceModule>()
    val subprojects = arrayListOf<Project>()
    val targetModules = arrayListOf<Module.TargetModule>()


}