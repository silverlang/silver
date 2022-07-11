package database.cache

object CacheCodes{
    const val HEADER_START: Byte = 0xFE.toByte()
    const val HEADER_END: Byte = 0xFF.toByte()
    const val PAYLOAD_SIZE: Byte = 0xF0.toByte()
    const val PAYLOAD_START: Byte = 0xEE.toByte()
    const val PAYLOAD_END: Byte = 0xEF.toByte()

    const val NAME: Byte = 0xa0.toByte()
    const val NAME_LENGTH: Byte = 0xa1.toByte()

    const val SECTION_START: Byte = 0x1E.toByte()
    const val SECTION_SIZE: Byte = 0x10.toByte()
    const val SECTION_END: Byte = 0x1F.toByte()

    const val GROUP_START: Byte = 0x2F.toByte()
    const val GROUP_SIZE: Byte = 0x20.toByte()
    const val GROUP_END: Byte = 0x2F.toByte()

    const val UNIT_START: Byte = 0x3F.toByte()
    const val UNIT_SIZE: Byte = 0x30.toByte()
    const val UNIT_END: Byte = 0x3F.toByte()
}