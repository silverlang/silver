package project

import project.canon.CanonicalName
import project.canon.toCanonicalName
import project.files.File
import project.modules.Module
import project.modules.ModuleData
import java.nio.file.Path

class TestTargetModule(
    path: Path,
    project: Project
): Module.TargetModule(
    listOf(), listOf(), path.toCanonicalName(), path, project
) {
    override fun dump() {
        this.files.map {
            it as TestTargetFile
        }.forEach {
            it.dump()
        }
    }

    override fun toTargetData(): TestTargetModuleData =
        TestTargetModuleData(
            files.map { it as TestTargetFile }, children.map { it as TestTargetModule },
            canonicalName, path
        )
}

class TestTargetModuleData(
    files: List<TestTargetFile>,
    children: List<TestTargetModule>,
    canonName: CanonicalName,
    path: Path
): ModuleData.TargetModuleData(
    canonName,
    path,
    children.map { it.toData() },
    files.map { it.toData() }
)