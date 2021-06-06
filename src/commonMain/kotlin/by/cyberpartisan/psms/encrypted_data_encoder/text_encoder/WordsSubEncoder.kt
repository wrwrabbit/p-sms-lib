package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

class WordsSubEncoder(private val words: List<String>) : SubEncoder {
    override fun encode(currentValue: BigInteger, remainingSize: BigInteger): EncodeResult {
        val index = (currentValue % words.size).intValue()
        return EncodeResult(BigInteger(words.size), words[index])
    }
}