package by.cyberpartisan.psms.plain_data_encoder

interface PlainDataEncoderFactory {
    fun create(mode: Int) : PlainDataEncoder
    fun createBestEncoder(s: String) : PlainDataEncoder
}