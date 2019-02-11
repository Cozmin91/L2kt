package com.l2kt.commons.lang

object HexUtil {
    fun printData(data: ByteArray, len: Int): String {
        val result = StringBuilder()

        var counter = 0

        for (i in 0 until len) {
            if (counter % 16 == 0)
                result.append(fillHex(i, 4) + ": ")

            result.append(fillHex(data[i].toInt() and 0xff, 2) + " ")
            counter++
            if (counter == 16) {
                result.append("   ")

                var charpoint = i - 15
                for (a in 0..15) {
                    val t1 = data[charpoint++].toInt()

                    if (t1 in 32..127)
                        result.append(t1.toChar())
                    else
                        result.append('.')
                }

                result.append("\n")
                counter = 0
            }
        }

        val rest = data.size % 16
        if (rest > 0) {
            for (i in 0 until 17 - rest)
                result.append("   ")

            var charpoint = data.size - rest
            for (a in 0 until rest) {
                val t1 = data[charpoint++].toInt()

                if (t1 in 32..127)
                    result.append(t1.toChar())
                else
                    result.append('.')
            }

            result.append("\n")
        }
        return result.toString()
    }

    fun fillHex(data: Int, digits: Int): String {
        var number = Integer.toHexString(data)

        for (i in number.length until digits) {
            number = "0$number"
        }

        return number
    }

    fun printData(raw: ByteArray): String {
        return printData(raw, raw.size)
    }
}