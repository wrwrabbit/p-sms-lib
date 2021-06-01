package by.cyberpartisan.psms.encrypted_data_encoder

interface EncryptedDataEncoderFactory {
    fun create(schemeId: Int) : EncryptedDataEncoder
}