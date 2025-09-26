package by.cyberpartisan.psms.plain_data_encoder

@ExperimentalUnsignedTypes
class ShortCp1251Cyrillic : ShortEncoder() {
    override fun encodeChar(char: Char): Code {
        val value = when (char.code) {
            in 0x20..0x7E -> char.lowercaseChar().code
            in 'а'.code..'я'.code -> char.code - 'а'.code // special chars
            'ё'.code -> 0x7F // del
            in 'А'.code..'И'.code -> char.code - 'А'.code + 'A'.code // A - I
            'Ё'.code -> 'J'.code
            in 'К'.code..'Щ'.code -> char.code - 'К'.code + 'K'.code // K - Z
            in 'Ъ'.code..'Ь'.code, 'Й'.code -> char.code - 'А'.code // as lower
            'Э'.code -> '&'.code
            'Ю'.code -> '^'.code
            'Я'.code -> '~'.code
            else -> '?'.code
        }
        return Code(value, decodingShifting)
    }


    override fun decodeChar(code: Int): Char {
        return when(code) {
            in 0x00 until 0x20 -> (code + 'а'.code).toChar() // special chars
            0x7F -> 'ё' // del
            in 'A'.code..'I'.code -> (code - 'A'.code + 'А'.code).toChar()
            'J'.code -> 'Ё'
            in 'K'.code..'Z'.code -> (code - 'K'.code + 'К'.code).toChar()
            '&'.code -> 'Э'
            '^'.code -> 'Ю'
            '~'.code -> 'Я'
            in 0x00..0x7E -> code.toChar()
            else -> '?'
        }
    }

    override fun getMode(): Int = Mode.SHORT_CP1251_PREFER_CYRILLIC.ordinal
}