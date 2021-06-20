package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

class Cp1251Test : AbstractPlainDataEncoderTest() {
    override fun encoder(): PlainDataEncoder = Cp1251()

    @Test
    fun testEmpty() {
        testEncodeDecode("", bytes())
    }

    @Test
    fun testSingleChar() {
            testEncodeDecode("ё", bytes(0xB8))
    }

    @Test
    fun testMultipleChar() {
        testEncodeDecode("µ…x", bytes(0xB5, 0x85, 0x78))
    }

    @Test
    fun testFullCharset() {
        val src = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ !\"#\$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~ЂЃ‚ѓ„…†‡€‰Љ‹ЊЌЋЏ ЎўЈ¤Ґ¦§©Є«¬\u00AD®Ї"
        val dest = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ !\"#\$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~ЂЃ‚ѓ„…†‡€‰Љ‹ЊЌЋЏ ЎўЈ¤Ґ¦§©Є«¬\u00AD®Ї"
        testEncodeDecode(src, expectedDecoded = dest)
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals('?'.code.toByte(), encoder().encode("π")[0], "Incorrect unknown char encoding.")
    }
}