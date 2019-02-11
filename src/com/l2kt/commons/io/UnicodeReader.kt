package com.l2kt.commons.io

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PushbackInputStream
import java.io.Reader

/**
 * Generic unicode textreader, which will use BOM mark to identify the encoding to be used. If BOM is not found then use a given default encoding. System default is used if: BOM mark is not found and defaultEnc is NULL Usage pattern: String defaultEnc = "ISO-8859-1"; // or NULL to use system default
 * FileInputStream fis = new FileInputStream(file); Reader in = new UnicodeReader(fis, defaultEnc);
 */
class UnicodeReader(`in`: InputStream, val defaultEncoding: String) : Reader() {
    private val internalIn: PushbackInputStream
    private var internalIn2: InputStreamReader? = null

    val encoding: String?
        get() = if (internalIn2 == null) null else internalIn2!!.encoding

    init {
        internalIn = PushbackInputStream(`in`, BOM_SIZE)
    }

    /**
     * Read-ahead four bytes and check for BOM marks. Extra bytes are unread back to the stream, only BOM bytes are skipped.
     * @throws IOException
     */
    @Throws(IOException::class)
    protected fun init() {
        if (internalIn2 != null)
            return

        val encoding: String?
        val bom = ByteArray(BOM_SIZE)
        val n: Int
        val unread: Int
        n = internalIn.read(bom, 0, bom.size)

        if (bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte()) {
            encoding = "UTF-8"
            unread = n - 3
        } else if (bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte()) {
            encoding = "UTF-16BE"
            unread = n - 2
        } else if (bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte()) {
            encoding = "UTF-16LE"
            unread = n - 2
        } else if (bom[0] == 0x00.toByte() && bom[1] == 0x00.toByte() && bom[2] == 0xFE.toByte() && bom[3] == 0xFF.toByte()) {
            encoding = "UTF-32BE"
            unread = n - 4
        } else if (bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() && bom[2] == 0x00.toByte() && bom[3] == 0x00.toByte()) {
            encoding = "UTF-32LE"
            unread = n - 4
        } else {
            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEncoding
            unread = n
        }

        if (unread > 0)
            internalIn.unread(bom, n - unread, unread)

        // Use given encoding
        internalIn2 = encoding?.let { InputStreamReader(internalIn, it) } ?: InputStreamReader(internalIn)
    }

    @Throws(IOException::class)
    override fun close() {
        init()
        internalIn.close()
        internalIn2!!.close()
    }

    @Throws(IOException::class)
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        init()
        return internalIn2!!.read(cbuf, off, len)
    }

    companion object {
        private val BOM_SIZE = 4
    }
}