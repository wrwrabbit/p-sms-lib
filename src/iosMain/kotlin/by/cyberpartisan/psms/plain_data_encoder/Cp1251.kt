package by.cyberpartisan.psms.plain_data_encoder

@ExperimentalUnsignedTypes
actual class Cp1251 : PlainDataEncoder {
    actual override fun encode(s: String): ByteArray {
        TODO("Not yet implemented")
    }

    actual override fun decode(data: ByteArray): String {
        TODO("Not yet implemented")
    }

    override fun getMode(): Int {
        TODO("Not yet implemented")
    }
}