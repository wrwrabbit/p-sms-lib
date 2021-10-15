package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import by.cyberpartisan.psms.encrypted_data_encoder.AbstractEncryptedDataEncoderTest
import by.cyberpartisan.psms.encrypted_data_encoder.CyrillicBase64
import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class TextEncoderTest : AbstractEncryptedDataEncoderTest() {
    override fun getEncoder(): EncryptedDataEncoder = TextEncoder.instance

    @Test
    fun printTest() {
        for (i in 1..10) {
            val str = TextEncoder.instance.encode(Random.Default.nextBytes(48))
            println(str)
        }
    }

    @Test
    fun testEmpty() {
        testEncodeDecode(ByteArray(0), "")
    }

    @Test
    fun testZero() {
        testEncodeDecode(ByteArray(1), null)
    }

    @Test
    fun testSingle() {
        testEncodeDecode(byteArrayOf(10), null)
    }

    @Test
    fun testDouble() {
        testEncodeDecode(byteArrayOf(10, 10), null)
    }

    @Test
    fun testTriple() {
        testEncodeDecode(byteArrayOf(10, 10, 10), null)
    }

    @Test
    fun testDecode() {
        val encoder = getEncoder()
        val decoded = encoder.decode("мурлыкой поссорено Кремню зоман маслиновую Эндрю Их Дюзам бакам щерим Фыркавшее топик Рюшным газует этологе чай Зарею полбу) Ухудшились галер")
        assertNotNull(decoded)
    }

    @Test
    fun testRandom() {
        for (i in 1..1000) {
            testEncodeDecode(Random.Default.nextBytes(48), null)
        }
    }

    override fun compareData(expected: List<Byte>, decoded: List<Byte>): Boolean {
        return expected == decoded.slice(decoded.size - expected.size until decoded.size)
    }

}