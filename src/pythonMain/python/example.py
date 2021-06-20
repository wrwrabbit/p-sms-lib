import p_sms
encrypted = p_sms.encode("str", "key", 0)
print(encrypted)
print(p_sms.isEncrypted("str", "key"))
print(p_sms.tryDecode(encrypted, "key"))
print(p_sms.tryDecode("raw", "key"))