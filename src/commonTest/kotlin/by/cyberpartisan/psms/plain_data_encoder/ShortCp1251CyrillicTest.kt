package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class ShortCp1251CyrillicTest : AbstractShortEncoderTest() {
    override fun encoder(): PlainDataEncoder = ShortCp1251Cyrillic()

    @Test
    fun testEmpty() {
        testEncodeDecode("", bytes(0x80))
    }

    @Test
    fun testSingleChar() {
        testEncodeDecode("ё", bytes(0xFF))
    }

    @Test
    fun testMultipleChar() {
        testEncodeDecode("xX", bytes(0xF1, 0xE2), "xx")
    }

    @Test
    fun testAShifting() {
        testShifting("aбвгдежз")
    }

    @Test
    fun testFullCharset() {
        val src = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ !\"#\$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~"
        val dest = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИйКЛМНОПРСТУФХЦЧШЩъыьЭЮЯabcdefghijklmnopqrstuvwxyz" +
                "abcdefghijklmnopqrstuvwxyz !\"#\$%Э'()*+,-./0123456789:;<=>?@[\\]Ю_`{|}Я"
        testEncodeDecode(src, expectedDecoded = dest)
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals(unknownCharEncoding(), encoder().encode("π")[0], "Incorrect unknown char encoding.")
    }
}