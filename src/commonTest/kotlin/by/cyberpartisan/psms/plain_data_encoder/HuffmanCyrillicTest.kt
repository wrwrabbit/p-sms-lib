package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

class HuffmanCyrillicTest : AbstractNotAlignedEncoderTest() {
    override fun encoder(): PlainDataEncoder = HuffmanCyrillic()

    @Test
    fun testEmpty() {
        testEncodeDecode("", bytes(0x80))
    }

    @Test
    fun testSingleChar() {
        testEncodeDecode("Ё", bytes(0x8D, 0xF3, 0xC0))
    }

    @Test
    fun testMultipleChar() {
        testEncodeDecode("Ёё", bytes(0x8D, 0xF3, 0x8A, 0xF0))
    }

    @Test
    fun testAShifting() {
        testShifting("aбвгдежз")
    }

    @Test
    fun testFullCharset() {
        val src = " оеиантсрвлкмдпуяы,гзьбчй.хжюцшщфэ-ВСПё10КН)А(М2РОeИТДo:Бai9Гr\"n53ФtЕsЛ4lъУ8Э6?З7u!Чc;ХЯmdШIhSgpCTAMPbk%/yЖBGfЦXNЮwEvDFVRLOWxHz№YUJ=ЙK+][j*Q’'Ы\$Ь&>ЩZqЁІ@іЎў\n"
        val dest = " оеиантсрвлкмдпуяы,гзьбчй.хжюцшщфэ-ВСПё10КН)А(М2РОeИТДo:Бai9Гr\"n53ФtЕsЛ4lъУ8Э6?З7u!Чc;ХЯmdШIhSgpCTAMPbk%/yЖBGfЦXNЮwEvDFVRLOWxHz№YUJ=ЙK+][j*Q’'Ы\$Ь&>ЩZqЁІ@іЎў\n"
        testEncodeDecode(src, expectedDecoded = dest)
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals(bytes(0x20, 0x78).toList(), encoder().encode("π").toList(), "Incorrect unknown char encoding.")
    }
}