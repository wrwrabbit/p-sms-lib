package by.cyberpartisan.psms.encrypted_data_encoder

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

abstract class AbstractEncryptedDataEncoderTest {
    protected fun testEncodeDecode(data: ByteArray, expectedEncoded: String?) {
        val encoder = getEncoder()
        val encoded = encoder.encode(data)
        if (expectedEncoded != null) {
            assertEquals(expectedEncoded, encoded, "Invalid encoded string.")
        }
        val decoded = encoder.decode(encoded)
        if (!compareData(data.toList(), decoded.toList())) {
            fail("Expected\t:${data.toList()}\n"
                + "Actual\t:${decoded.toList()}\n"
                + "Invalid decoded data.")
        }
    }

    protected abstract fun getEncoder(): EncryptedDataEncoder

    protected open fun compareData(expected: List<Byte>, decoded: List<Byte>) : Boolean {
        return true
    }
}