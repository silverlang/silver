package database.cache.input

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import arrow.core.valid
import database.cache.CacheCodes
import database.data.DataElement
import database.data.Header
import database.data.Payload
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import okio.BufferedSource
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toOkioPath
import project.Project
import project.canon.CanonicalName
import project.files.File
import project.files.FileData
import java.nio.charset.Charset
import java.nio.file.Path

class CacheInputFile(
    canonicalName: CanonicalName,
    path: Path,
    project: Project
): File.ReadableFile<DataElement.Section>(
    canonicalName,
    path,
    project
) {

    @OptIn(ExperimentalSerializationApi::class)
    override val input: Option<DataElement.Section>
        get() = FileSystem.SYSTEM.read(path.toOkioPath()){
            Cbor.decodeFromByteArray<DataElement.Section>(readByteArray())
        }.some()

    override fun toData(): CacheInputFileData =
        CacheInputFileData(canonName, path)
}

class CacheInputFileData(
    canonicalName: CanonicalName,
    path: Path
): FileData(
    canonicalName, path
)