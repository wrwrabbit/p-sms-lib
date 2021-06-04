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
    fun testInvalidEncoding() {
        assertEquals('?'.toByte(), encoder().encode("π")[0], "Incorrect unknown char encoding.")
    }
}