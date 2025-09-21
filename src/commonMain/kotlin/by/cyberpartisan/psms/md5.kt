package by.cyberpartisan.psms

import korlibs.crypto.MD5

public fun md5(arr: ByteArray): ByteArray {
    val md5 = MD5()
    return md5.update(arr).digest().bytes
}