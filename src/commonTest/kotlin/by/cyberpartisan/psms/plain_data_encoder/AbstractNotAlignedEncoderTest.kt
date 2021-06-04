package by.cyberpartisan.psms.plain_data_encoder

abstract class AbstractNotAlignedEncoderTest : AbstractPlainDataEncoderTest() {
    fun testShifting(str: String) {
        for (i in str.indices) {
            testEncodeDecode(str.substring(0..i))
        }
    }
}