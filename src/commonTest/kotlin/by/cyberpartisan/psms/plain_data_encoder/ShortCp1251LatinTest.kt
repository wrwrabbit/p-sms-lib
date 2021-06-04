package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class ShortCp1251LatinTest : AbstractShortEncoderTest() {
    override fun encoder(): PlainDataEncoder = ShortCp1251Latin()

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
        testEncodeDecode("бБ", expectedDecoded = "бб")
    }

    @Test
    fun testAShifting() {
        testShifting("aбвгдежз")
    }

    @Test
    fun testFullCharset() {
        val src = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ !\"#\$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~"
        val dest = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяабвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ !\"#\$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~"
        testEncodeDecode(src, expectedDecoded = dest)
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals(unknownCharEncoding(), encoder().encode("π")[0], "Incorrect unknown char encoding.")
    }
}