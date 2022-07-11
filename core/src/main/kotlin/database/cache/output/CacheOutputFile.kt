package database.cache.output

import arrow.core.Option
import arrow.core.some
import database.data.DataElement
import okio.BufferedSink
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import project.Project
import project.canon.CanonicalName
import project.files.File
import project.files.FileData
import java.nio.file.Path
import database.cache.CacheCodes
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray

class CacheOutputFile(
    canonicalName: CanonicalName,
    path: Path,
    project: Project,
    val data: Option<DataElement.Section>
): File.TargetFile(canonicalName, path, project) {
    override fun dump() {
        FileSystem.SYSTEM.write(path.toOkioPath()){
            data.tap {
//                val tag = encoder.encode(it)
                val ba = Cbor.encodeToByteArray(it)
                write(ba)
            }
        }
    }

    override fun toData(): CacheFileData =
        CacheFileData(canonName, path)
}

class CacheFileData(
    canonicalName: CanonicalName,
    path: Path
): FileData.TargetFileData(
    canonicalName,
    path
)
