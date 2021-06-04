package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class AsciiTest : AbstractShortEncoderTest() {
    override fun encoder(): PlainDataEncoder = Ascii()

    @Test
    fun testEmpty() {
        testEncodeDecode("", bytes(0x80))
    }

    @Test
    fun testSingleChar() {
        testEncodeDecode("\n", byteArrayOf(0x15))
    }

    @Test
    fun testMultipleChar() {
        testEncodeDecode("bB", bytes(0xC5, 0x0A))
    }

    @Test
    fun testNullShifting() {
        testShifting("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007")
    }

    @Test
    fun testFullCharset() {
        val src = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ !\"#\$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~\r\n"
        val dest = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ !\"#\$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~\r\n"
        testEncodeDecode(src, expectedDecoded = dest)
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals(unknownCharEncoding(), encoder().encode("π")[0], "Incorrect unknown char encoding.")
        assertEquals(unknownCharEncoding(), encoder().encode("ё")[0], "Incorrect unknown char encoding.")
    }
}