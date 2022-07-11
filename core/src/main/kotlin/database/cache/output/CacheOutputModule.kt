package database.cache.output

import project.Project
import project.canon.CanonicalName
import project.modules.Module
import project.modules.ModuleData
import java.nio.file.Path

class CacheOutputModule(
    canonicalName: CanonicalName,
    path: Path,
    project: Project
): Module.TargetModule(
    emptyList(),
    emptyList(),
    canonicalName,
    path,
    project
) {
    override fun dump() {
        children.forEach {
            if(it is CacheOutputModule){
                it.dump()
            }
        }
        files.forEach {
            if(it is CacheOutputFile){
                it.dump()
            }
        }
    }

    override fun toData(): CacheModuleData =
        CacheModuleData(
            canonicalName,
            path,
            children.filterIsInstance<CacheOutputModule>().map { it.toData() },
            files.filterIsInstance<CacheOutputFile>().map { it.toData() }
        )

}

class CacheModuleData(
    canonicalName: CanonicalName,
    path: Path,
    children: List<CacheModuleData>,
    files: List<CacheFileData>
): ModuleData.TargetModuleData(
    canonicalName, path, children, files
)