package by.cyberpartisan.psms.plain_data_encoder

abstract class AbstractShortEncoderTest : AbstractNotAlignedEncoderTest() {
    protected fun unknownCharEncoding(): Byte = ('?'.code shl 1 or 1).toByte()
}