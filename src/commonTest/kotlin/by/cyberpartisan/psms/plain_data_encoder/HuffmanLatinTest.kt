package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

class HuffmanLatinTest : AbstractNotAlignedEncoderTest() {
    override fun encoder(): PlainDataEncoder = HuffmanLatin()

    @Test
    fun testEmpty() {
        testEncodeDecode("", bytes(0x80))
    }

    @Test
    fun testSingleChar() {
        testEncodeDecode("x", bytes(0x69, 0x40))
    }

    @Test
    fun testMultipleChar() {
        testEncodeDecode("Xx", bytes(0x8C, 0x2D, 0xA5))
    }

    @Test
    fun testAShifting() {
        testShifting("abcdefgh")
    }

    @Test
    fun testFullCharset() {
        val src = " etaoinsrhldcumfpgwybvkTIS'AxC1-MBH0EWPRL9DNjqFOG2.8z53J476UKYV,£/–%Q\$ZX&é()=+:*è<″×üáµçíöóα>βäàñâô′_êāï[γëş;]μÖúøχ|šΔãî\\Éε@θΒćΕδσčÅý{#¥±λûφřπŠνρòτžěåÿÁœηοìùΠζΓČ§κň}łţśÎωńÀđÜßØψĀΩÞÓÇΨŝΣŚþΘ¿ΟīŽůΦξÚę⅞ÈË?ŷÄŞõυÑăðÍ´¢ūźŁō\"ŕΖÂŵēąŸιΥħņĺĉĹĚŘťŶΩ’żÔÕÊ‘ď\n"
        val dest = " etaoinsrhldcumfpgwybvkTIS'AxC1-MBH0EWPRL9DNjqFOG2.8z53J476UKYV,£/–%Q\$ZX&é()=+:*è<″×üáµçíöóα>βäàñâô′_êāï[γëş;]μÖúøχ|šΔãî\\Éε@θΒćΕδσčÅý{#¥±λûφřπŠνρòτžěåÿÁœηοìùΠζΓČ§κň}łţśÎωńÀđÜßØψĀΩÞÓÇΨŝΣŚþΘ¿ΟīŽůΦξÚę⅞ÈË?ŷÄŞõυÑăðÍ´¢ūźŁō\"ŕΖÂŵēąŸιΥħņĺĉĹĚŘťŶΩ’żÔÕÊ‘ď\n"
        testEncodeDecode(src, expectedDecoded = dest)
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals(bytes(0x68, 0xF0).toList(), encoder().encode("ў").toList(), "Incorrect unknown char encoding.")
    }
}