package by.cyberpartisan.psms.plain_data_encoder

abstract class NotAlignedEncoder: PlainDataEncoder {
    data class Code(var value: Int, var size: Int)
    abstract fun encodeChar(char: Char): Code
    abstract fun beforeDecode()
    abstract fun processDecodingValue(value: Int)
    abstract fun getDecodedString(): String
    abstract val decodingShifting: Int

    final override fun encode(s: String): ByteArray {
        var bitsLeftInLastByte = 0
        val result = ArrayList<Byte>()
        for (char in s) {
            val code = encodeChar(char).copy()
            while (code.size > 0) {
                if (bitsLeftInLastByte == 0) {
                    result.add(0)
                    bitsLeftInLastByte = 8
                }
                val valueForByte = if (code.size > bitsLeftInLastByte)
                        (code.value shr code.size - bitsLeftInLastByte) and 0xFF
                        else code.value shl bitsLeftInLastByte - code.size
                result[result.size - 1] = (result.last().toInt() or valueForByte).toByte()
                if (code.size < bitsLeftInLastByte) {
                    bitsLeftInLastByte -= code.size
                    code.size = 0
                } else {
                    code.size -= bitsLeftInLastByte
                    code.value = code.value and (0xFF shl code.size).inv()
                    bitsLeftInLastByte = 0
                }
            }
        }
        if (bitsLeftInLastByte == 0) {
            result.add(0x80.toByte())
        } else {
            result[result.size - 1] = (result.last().toInt() or (1 shl (bitsLeftInLastByte - 1))).toByte()
        }
        return result.toByteArray()
    }

    final override fun decode(data: ByteArray): String {
        beforeDecode()
        var currentOffset = 0
        while (data.size * 8 - currentOffset >= decodingShifting) {
            val byteIndex = currentOffset / 8
            val byteOffset = currentOffset % 8
            if (byteIndex == data.size - 1 && 8 - (getLeastSignificantBitOffset(data[byteIndex]) + 1) < decodingShifting) {
                break
            }
            var value = (data[byteIndex].toInt() and (0xFF shr byteOffset))
            if (decodingShifting < 8 - byteOffset) {
                value = value shr 8 - byteOffset - decodingShifting
            } else if (8 - byteOffset < decodingShifting) {
                val shifting = decodingShifting - (8 - byteOffset)
                value = value shl shifting
                value = value or ((data[byteIndex + 1].toInt() and 0xFF) shr (8 - shifting))
            }
            currentOffset += decodingShifting
            processDecodingValue(value)
        }
        return getDecodedString()
    }

    private fun getLeastSignificantBitOffset(value: Byte): Int {
        var index = 0
        var intValue = value.toInt() and 0xFF
        while (intValue and 1 == 0) {
            index++
            intValue = intValue shr 1
        }
        return index
    }
}