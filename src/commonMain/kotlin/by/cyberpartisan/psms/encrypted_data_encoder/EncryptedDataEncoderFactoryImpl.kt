package by.cyberpartisan.psms.encrypted_data_encoder

import by.cyberpartisan.psms.encrypted_data_encoder.text_encoder.TextEncoder

class EncryptedDataEncoderFactoryImpl: EncryptedDataEncoderFactory {
    override fun create(schemeId: Int) : EncryptedDataEncoder {
        return when (schemeId) {
            Scheme.BASE64.ordinal -> Base64()
            Scheme.CYRILLIC_BASE64.ordinal -> CyrillicBase64()
            Scheme.TEXT.ordinal -> TextEncoder.instance
            else -> Base64()
        }
    }
}