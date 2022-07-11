package database.data

import kotlinx.serialization.Polymorphic

@kotlinx.serialization.Serializable
sealed class DataElement{
    abstract val name: String
    abstract val size: Int

    @kotlinx.serialization.Serializable
    class Section(
        override val name: String,
        val children: MutableList<DataElement> = mutableListOf(),
    ): DataElement(){
        override val size: Int = children.sumOf { it.size}

        override fun toString(): String {
            return buildString {
                append("\"$name\": {")
                for(child in children){
                    append("$child")
                }
                append("}")
            }
        }
    }

    @kotlinx.serialization.Serializable
    class Group(
        override val name: String,
        val children: MutableList<Unit> = mutableListOf()
    ): DataElement(){
        override val size: Int = children.sumOf { it.size }

        override fun toString(): String {
            return buildString {
                append("group \"$name\": {")
                for(child in children){
                    append(child)
                }
                append("}")
            }
        }
    }

    @kotlinx.serialization.Serializable
    class Unit(override val name: String, val data: ByteArray): DataElement(){
        override val size: Int = data.size

        override fun toString(): String {
            return buildString {
                append("unit \"$name\": ${data.joinToString()}")
            }
        }
    }
}
