package by.cyberpartisan.psms.plain_data_encoder

class PlainDataEncoderFactoryImpl: PlainDataEncoderFactory {
    @ExperimentalUnsignedTypes
    override fun create(mode: Int) : PlainDataEncoder {
        return when (mode) {
            Mode.LATIN.ordinal -> ShortCp1251Latin()
            Mode.CYRILLIC.ordinal -> ShortCp1251Cyrillic()
            Mode.CP1251.ordinal -> Cp1251()
            Mode.HUFFMAN_CYRILLIC.ordinal -> HuffmanCyrillic()
            else -> Utf8()
        }
    }

    @ExperimentalUnsignedTypes
    override fun createBestEncoder(s: String) : PlainDataEncoder {
        var minSize = Int.MAX_VALUE
        var minEncoder: PlainDataEncoder = Utf8()
        for (mode in Mode.values()) {
            val encoder = create(mode.ordinal)
            val encoded = encoder.encode(s)
            val decoded = encoder.decode(encoded)
            if (decoded == s && encoded.size < minSize) {
                minSize = encoded.size
                minEncoder = encoder
            }
        }
        return minEncoder
    }
}