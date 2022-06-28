package project.canon

import arrow.core.Option
import arrow.core.some
import arrow.core.toOption
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

data class CanonicalName(
    val name: String,
    val parent: Option<CanonicalName>
){
    override fun toString(): String =
        buildString {
            parent.tap {
                append(it.toString())
                append('.')
            }
            append(name)
        }

    operator fun plus(other: CanonicalName): CanonicalName =
        CanonicalName(other.name, this.some())

    operator fun plus(other: String): CanonicalName =
        CanonicalName(other, this.some())
}

fun Path.toCanonicalName(): CanonicalName =
    CanonicalName(
        this.nameWithoutExtension,
        this.parent?.toCanonicalName().toOption()
    )

fun String.toCanonicalName(): CanonicalName =
    kotlin.io.path.Path(this).toCanonicalName()