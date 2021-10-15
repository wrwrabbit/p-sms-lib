package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

const val size = 60 * 24 + 365

class DateTimeSubEncoder: SubEncoder {
    @Suppress("RegExpRedundantEscape")
    private val regex = Regex("(\\d\\d)[\\.:](\\d\\d)")
    private val monthSizes = arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    override fun encode(currentValue: BigInteger): EncodeResult {
        val index = (currentValue % size).intValue()
        val word = if (index < 60 * 24) encodeAsTime(index) else encodeAsDate(index - 60 * 24)
        return EncodeResult(BigInteger(size), word)
    }

    private fun encodeAsTime(value: Int): String {
        val hourStr = (value / 60).toString().padStart(2, '0')
        val minuteStr = (value % 60).toString().padStart(2, '0')
        return "${hourStr}:${minuteStr}"
    }

    private fun encodeAsDate(value: Int): String {
        var day = value
        var month = 0
        while (day >= monthSizes[month]) {
            day -= monthSizes[month]
            month++
        }
        val dayStr = (day + 1).toString().padStart(2, '0')
        val monthStr = (month + 1).toString().padStart(2, '0')
        return "${dayStr}.${monthStr}"
    }

    override fun decode(str: String, index: Int): DecodeResult? {
        if (str.length - index < 5) {
            return null
        }
        val substring = str.subSequence(index, index + 5)
        val matchResult = regex.matchEntire(substring) ?: return null
        val parts = listOf(matchResult.groupValues[1].toInt(), matchResult.groupValues[2].toInt())
        val value = (if (substring[2] == ':') decodeTime(parts) else decodeDate(parts)) ?: return null
        val newPosition = if (str.length - index >= 6 && str[index + 5] == ' ') index + 6 else index + 5
        return DecodeResult(size, value, newPosition)
    }

    private fun decodeTime(parts: List<Int>): Int? {
        return if (parts.size == 2 && parts[0] < 24 && parts[1] < 60) {
            parts[0] * 60 + parts[1]
        } else {
            null
        }
    }

    private fun decodeDate(parts: List<Int>): Int? {
        if (parts.size != 2) {
            return null
        }
        val day = parts[0]
        val month = parts[1]
        return if (month in 1..monthSizes.size && day in 1..monthSizes[parts[1] - 1]) {
            monthSizes.slice(0 until month - 1).sum() + (day - 1) + 60 * 24
        } else {
            null
        }
    }
}