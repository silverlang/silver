package project

import project.canon.CanonicalName
import project.canon.toCanonicalName
import project.files.File
import project.files.FileData
import java.nio.file.Path
import kotlin.io.path.bufferedWriter

class TestTargetFile(
    path: Path,
    project: Project,
    val data: String
): File.TargetFile(
    path.toCanonicalName(),
    path,
    project,
) {
    override fun dump() {
        path.bufferedWriter().use {
            it.write(data)
        }
    }

    override fun toTargetData(): FileData.TargetFileData =
        TestTargetFileData(
            canonName, path, data
        )

}

class TestTargetFileData(
    canonicalName: CanonicalName,
    path: Path,
    val data: String
): FileData.TargetFileData(canonicalName, path)

fun TestTargetModule.makeTestTargetFile(name: String, data: String){
    TestTargetFile(path + name, project, data).also {
        it.finish()
        files.add(it)
    }
}

fun Project.makeTestTargetModule(name: String, block: TestTargetModule.()->Unit){
    TestTargetModule(path + "target" + name, this).apply(block).also {
        it.finish()
        targetModules.add(it)
    }
}