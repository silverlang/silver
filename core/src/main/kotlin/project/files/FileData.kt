package project.files

import arrow.core.Option
import project.canon.CanonicalName
import java.nio.file.Path

abstract class FileData(
    val canonName: CanonicalName,
    val path: Path,
){
    class SourceFileData(
        canonName: CanonicalName,
        path: Path,
        val input: Option<String>
    ): FileData(
        canonName, path
    )

    abstract class TargetFileData(
        canonName: CanonicalName,
        path: Path
    ): FileData(
        canonName, path
    )
}

