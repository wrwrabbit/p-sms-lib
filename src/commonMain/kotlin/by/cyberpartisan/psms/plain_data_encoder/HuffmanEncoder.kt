package by.cyberpartisan.psms.plain_data_encoder

abstract class HuffmanEncoder : NotAlignedEncoder() {
    abstract val charToCode: Map<Char, Code>

    private val codeToChar by lazy { charToCode.entries.associateBy({ it.value }) {it.key} }
    private val stringBuilder by lazy { StringBuilder() }
    private val currentCode by lazy { Code(0, 0) }

    override fun encodeChar(char: Char): Code {
        return charToCode[char] ?: charToCode['?']!!
    }

    override fun beforeDecode() {
        stringBuilder.clear()
        clearCurrentCode()
    }

    private fun clearCurrentCode() {
        currentCode.value = 0
        currentCode.size = 0
    }

    override fun processDecodingValue(value: Int) {
        currentCode.value = (currentCode.value shl 1) or value
        currentCode.size++
        if (codeToChar.containsKey(currentCode)) {
            stringBuilder.append(codeToChar[currentCode])
            clearCurrentCode()
        }
    }

    final override fun getDecodedString(): String {
        return stringBuilder.toString()
    }

    final override val decodingShifting = 1
}