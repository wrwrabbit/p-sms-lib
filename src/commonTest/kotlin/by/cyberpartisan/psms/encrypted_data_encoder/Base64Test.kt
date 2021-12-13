package by.cyberpartisan.psms.encrypted_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class Base64Test : AbstractEncryptedDataEncoderTest() {
    override fun getEncoder(): EncryptedDataEncoder = Base64()

    @Test
    fun testEmpty() {
        testEncodeDecode(ByteArray(0), "")
    }

    @Test
    fun testSingle() {
        testEncodeDecode(byteArrayOf(0), "AA==")
    }

    @Test
    fun testLong() {
        testEncodeDecode(byteArrayOf(1, 2, 3), "AQID")
    }

    @Test
    fun testInvalidChars() {
        assertFails { Base64().decode("!") }
    }
}