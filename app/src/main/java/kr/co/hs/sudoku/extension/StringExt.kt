package kr.co.hs.sudoku.extension

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.String

object StringExt {
    val String.md5: String
        get() {
            val md5 = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
                .toString(16)
            val padding = "00000000000000000000000000000000".substring(0, 32 - md5.length)
            return (padding + md5).uppercase()
        }
}