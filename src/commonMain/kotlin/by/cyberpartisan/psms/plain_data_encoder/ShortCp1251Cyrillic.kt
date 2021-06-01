package by.cyberpartisan.psms.plain_data_encoder

@ExperimentalUnsignedTypes
class ShortCp1251Cyrillic : ShortEncoder() {
    override fun encodeChar(char: Char): Code {
        val value = when(char.toInt()) {
            in 0x20..0x7E -> char.toLowerCase().toInt()
            in 'а'.toInt()..'я'.toInt() -> char.toInt() - 'а'.toInt() // special chars
            'ё'.toInt() -> 0x7F // del
            in 'А'.toInt()..'И'.toInt() -> char.toInt() - 'А'.toInt() + 'A'.toInt() // A - I
            'Ё'.toInt() -> 'J'.toInt()
            in 'К'.toInt()..'Щ'.toInt() -> char.toInt() - 'К'.toInt() + 'K'.toInt() // K - Z
            in 'Ъ'.toInt()..'Ь'.toInt(), 'Й'.toInt() -> char.toInt() - 'А'.toInt() // as lower
            'Э'.toInt() -> '&'.toInt()
            'Ю'.toInt() -> '^'.toInt()
            'Я'.toInt() -> '~'.toInt()
            else -> '?'.toInt()
        }
        return Code(value, decodingShifting)
    }


    override fun decodeChar(code: Int): Char {
        return when(code) {
            in 0x00 until 0x20 -> (code + 'а'.toInt()).toChar() // special chars
            0x7F -> 'ё' // del
            in 'A'.toInt()..'I'.toInt() -> (code - 'A'.toInt() + 'А'.toInt()).toChar()
            'J'.toInt() -> 'Ё'
            in 'K'.toInt()..'Z'.toInt() -> (code - 'K'.toInt() + 'К'.toInt()).toChar()
            '&'.toInt() -> 'Э'
            '^'.toInt() -> 'Ю'
            '~'.toInt() -> 'Я'
            in 0x00..0x7E -> code.toChar()
            else -> '?'
        }
    }

    override fun getMode(): Int = Mode.CYRILLIC.ordinal
}