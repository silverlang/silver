package project

import kotlin.io.path.Path
import java.nio.file.Path as JPath

operator fun JPath.plus(other: JPath) =
    this.resolve(other)

operator fun JPath.plus(other: String) =
    this.resolve(Path(other))

operator fun String.plus(other: JPath) =
    Path(this).resolve(other)