package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

class WordsSubEncoder(private val words: List<String>) : SubEncoder {
    private val wordsToIndexMap: Map<String, Int> = words.mapIndexed{ i, word -> word to i}.toMap()

    override fun encode(currentValue: BigInteger): EncodeResult {
        val index = (currentValue % words.size).intValue()
        return EncodeResult(BigInteger(words.size), words[index])
    }

    override fun decode(str: String, index: Int): DecodeResult? {
        for (lastIndex in index+1..str.length) {
            val value = wordsToIndexMap[str.subSequence(index, lastIndex)]
            if (value != null) {
                val newPosition = if (lastIndex < str.length && str[lastIndex] == ' ') lastIndex + 1 else lastIndex
                return DecodeResult(BigInteger(words.size), value, newPosition)
            }
        }
        return null
    }
}