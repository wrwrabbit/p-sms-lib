package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

class Utf8Test : AbstractPlainDataEncoderTest() {
    override fun encoder(): PlainDataEncoder = Utf8()

    @Test
    fun testEmpty() {
        testEncodeDecode("", bytes())
    }

    @Test
    fun testSingleChar() {
        testEncodeDecode("ё", bytes(0xd1, 0x91))
    }

    @Test
    fun testMultipleChar() {
        testEncodeDecode("πў", bytes(0xCF, 0x80, 0xD1, 0x9E))
    }
}