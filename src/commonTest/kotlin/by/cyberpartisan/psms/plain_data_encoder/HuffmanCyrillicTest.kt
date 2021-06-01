package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

class HuffmanCyrillicTest {
    private fun testEncodeDecode(str: String, expectedEncoded: ByteArray?) {
        val encoder = HuffmanCyrillic()
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
        testEncodeDecode("Ё", byteArrayOf((0x8D).toByte(), (0xF3).toByte(), (0xC0).toByte()))
    }

    @Test
    fun testMultipleChar() {
        val encoder = HuffmanCyrillic()
        val encoded = encoder.encode("Ёё")
        val expected = byteArrayOf((0x8D).toByte(), (0xF3).toByte(), (0x8A).toByte(), (0xF0).toByte()).toList()
        assertEquals(expected, encoded.toList(), "Invalid encoding.")
        val decoded = encoder.decode(encoded)
        assertEquals("Ёё", decoded, "Invalid decoding.")
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
        val encoder = HuffmanCyrillic()
        val src = " оеиантсрвлкмдпуяы,гзьбчй.хжюцшщфэ-ВСПё10КН)А(М2РОeИТДo:Бai9Гr\"n53ФtЕsЛ4lъУ8Э6?З7u!Чc;ХЯmdШIhSgpCTAMPbk%/yЖBGfЦXNЮwEvDFVRLOWxHz№YUJ=ЙK+][j*Q’'Ы\$Ь&>ЩZqЁІ@іЎў\n"
        val dest = " оеиантсрвлкмдпуяы,гзьбчй.хжюцшщфэ-ВСПё10КН)А(М2РОeИТДo:Бai9Гr\"n53ФtЕsЛ4lъУ8Э6?З7u!Чc;ХЯmdШIhSgpCTAMPbk%/yЖBGfЦXNЮwEvDFVRLOWxHz№YUJ=ЙK+][j*Q’'Ы\$Ь&>ЩZqЁІ@іЎў\n"
        assertEquals(dest, encoder.decode(encoder.encode(src)), "Invalid charset.")
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals('?'.toByte(), Cp1251().encode("π")[0], "Incorrect unknown char encoding.")
    }
}