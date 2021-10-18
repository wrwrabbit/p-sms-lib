package by.cyberpartisan.psms

/*
    byte layout: cvvvmmmm
    c - isChannel
    v - version bits
    m - mode bits
*/

class MetaInfo(val mode: Int,
               val version: Int,
               val isChannel: Boolean) {
    companion object {
        fun parse(metaInfoByte: Byte): MetaInfo {
            val metaInfoInt = metaInfoByte.toInt()
            val mode = metaInfoInt and 0x0F
            val version = (metaInfoInt and 0x70) shr 4
            val isEncoded = (metaInfoInt and 0x80) != 0
            return MetaInfo(mode, version, isEncoded)
        }
    }

    fun toByte(): Byte {
        return (mode or (version shl 4) or ((if (isChannel) 1 else 0) shl 7)).toByte()
    }
}