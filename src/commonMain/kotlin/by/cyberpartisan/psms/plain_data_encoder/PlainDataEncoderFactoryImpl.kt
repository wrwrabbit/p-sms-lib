package by.cyberpartisan.psms.plain_data_encoder

class PlainDataEncoderFactoryImpl: PlainDataEncoderFactory {
    @ExperimentalUnsignedTypes
    override fun create(mode: Int) : PlainDataEncoder {
        return when (mode) {
            Mode.SHORT_CP1251_PREFER_LATIN.ordinal -> ShortCp1251Latin()
            Mode.SHORT_CP1251_PREFER_CYRILLIC.ordinal -> ShortCp1251Cyrillic()
            Mode.CP1251.ordinal -> Cp1251()
            Mode.ASCII.ordinal -> Ascii()
            Mode.HUFFMAN_CYRILLIC.ordinal -> HuffmanCyrillic()
            Mode.HUFFMAN_LATIN.ordinal -> HuffmanLatin()
            else -> Utf8()
        }
    }

    @ExperimentalUnsignedTypes
    override fun create(mode: Mode): PlainDataEncoder {
        return create(mode.ordinal)
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