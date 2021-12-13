package by.cyberpartisan.psms.encrypted_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CyrillicBase64Test : AbstractEncryptedDataEncoderTest() {
    override fun getEncoder(): EncryptedDataEncoder = CyrillicBase64()

    @Test
    fun testEmpty() {
        testEncodeDecode(ByteArray(0), "")
    }

    @Test
    fun testSingle() {
        testEncodeDecode(byteArrayOf(0), "ААяя")
    }

    @Test
    fun testLong() {
        testEncodeDecode(byteArrayOf(1, 2, 3), "АПЗГ")
    }

    @Test
    fun testInvalidChars() {
        assertFails { Base64().decode("Ъ") }
    }
}