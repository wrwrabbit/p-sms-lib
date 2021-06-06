package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

class DateTimeSubEncoder: SubEncoder {
    override fun encode(currentValue: BigInteger, remainingSize: BigInteger): EncodeResult {
        val size = 60 * 24 + 365
        val index = (currentValue % size).intValue()
        val word = if (index < 60 * 24) encodeAsTime(index) else encodeAsDate(index - 60 * 24)
        return EncodeResult(BigInteger(size), word)
    }

    private fun encodeAsTime(value: Int): String = "${value/60}:${value%60}"

    private fun encodeAsDate(value: Int): String {
        val monthSizes = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var day = value
        var month = 0
        while (value < monthSizes[month]) {
            day -= monthSizes[month]
            month++
        }
        return "${day}.${month}"
    }


}