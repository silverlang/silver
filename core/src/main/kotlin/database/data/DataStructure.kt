package database.data

data class Header(val payloadSize: Int)

data class Payload(
    val header: Header,
    val root: DataElement.Section
)