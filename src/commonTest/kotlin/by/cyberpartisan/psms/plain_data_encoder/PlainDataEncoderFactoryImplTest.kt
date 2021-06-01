package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class PlainDataEncoderFactoryImplTest {
    private fun testCreateBest(str: String, expectedMode: Mode) {
        val encoder = PlainDataEncoderFactoryImpl().createBestEncoder(str)
        assertEquals(expectedMode, Mode.values()[encoder.getMode()], "Invalid mode.")
    }

    @Test
    fun testCreateUtf8() {
        testCreateBest("π", Mode.UTF_8)
    }

    @Test
    fun testCreateCp1251() {
        testCreateBest("абвгдАБВГДabcdeABCDE", Mode.CP1251)
    }

    @Test
    fun testCreateShortLatin() {
        testCreateBest("абвгдabcdeABCDE", Mode.LATIN)
    }

    @Test
    fun testCreateShortCyrillic() {
        testCreateBest("абвгдАБВГДabcde", Mode.CYRILLIC)
    }

    @Test
    fun testCreateHuffmanCyrillic() {
        testCreateBest("О о о о о ", Mode.HUFFMAN_CYRILLIC)
    }
}