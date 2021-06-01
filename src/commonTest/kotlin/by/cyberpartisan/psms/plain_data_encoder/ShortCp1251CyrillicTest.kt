package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class ShortCp1251CyrillicTest {
    private fun testEncodeDecode(str: String, expectedEncoded: ByteArray?) {
        val encoder = ShortCp1251Cyrillic()
        val encoded = encoder.encode(str)
        if (expectedEncoded != null) {
            assertEquals(expectedEncoded.toList(), encoded.toList(), "Invalid encoding.")
        }
        val decoded = encoder.decode(encoded)
        assertEquals(str, decoded, "Invalid decoding.")
    }

    @Test
    fun testEmpty() {
        testEncodeDecode("", byteArrayOf((0x80).toByte()))
    }

    @Test
    fun testSingleChar() {
        testEncodeDecode("ё", byteArrayOf((0xFF).toByte()))
    }

    @Test
    fun testMultipleChar() {
        val encoder = ShortCp1251Cyrillic()
        val encoded = encoder.encode("xX")
        assertEquals(byteArrayOf((0xF1).toByte(), (0xE2).toByte()).toList(), encoded.toList(), "Invalid encoding.")
        val decoded = encoder.decode(encoded)
        assertEquals("xx", decoded, "Invalid decoding.")
    }

    @Test
    fun testAShifting() {
        testEncodeDecode("a", null)
        testEncodeDecode("aб", null)
        testEncodeDecode("aбв", null)
        testEncodeDecode("aбвг", null)
        testEncodeDecode("aбвгд", null)
        testEncodeDecode("aбвгде", null)
        testEncodeDecode("aбвгдеж", null)
        testEncodeDecode("aбвгдежз", null)
    }

    @Test
    fun testFullCharset() {
        val encoder = ShortCp1251Cyrillic()
        val src = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ !\"#\$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~"
        val dest = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИйКЛМНОПРСТУФХЦЧШЩъыьЭЮЯabcdefghijklmnopqrstuvwxyz" +
                "abcdefghijklmnopqrstuvwxyz !\"#\$%Э'()*+,-./0123456789:;<=>?@[\\]Ю_`{|}Я"
        assertEquals(dest, encoder.decode(encoder.encode(src)), "Invalid charset.")
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals('?'.toByte(), Cp1251().encode("π")[0], "Incorrect unknown char encoding.")
    }
}