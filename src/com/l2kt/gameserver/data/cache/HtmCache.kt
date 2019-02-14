package com.l2kt.gameserver.data.cache

import com.l2kt.commons.io.UnicodeReader
import com.l2kt.commons.logging.CLogger
import java.io.BufferedReader
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.util.*

/**
 * A cache storing HTMs content.<br></br>
 * <br></br>
 * HTMs are loaded lazily, on request, then their [String] content can be retrieved using path hashcode.
 */
object HtmCache {

    private val _htmCache = HashMap<Int, String>()
    private val _htmFilter = HtmFilter()
    private val LOGGER = CLogger(HtmCache::class.java.name)

    /**
     * Cleans the HTM cache.
     */
    fun reload() {
        LOGGER.info("HtmCache has been cleared ({} entries).", _htmCache.size)

        _htmCache.clear()
    }

    /**
     * Loads and stores the HTM file content.
     * @param file : The file to be cached.
     * @return the content of the file under a [String].
     */
    private fun loadFile(file: File): String? {
        try {
            FileInputStream(file).use { fis ->
                UnicodeReader(fis, "UTF-8").use { ur ->
                    BufferedReader(ur).use { br ->
                        val sb = StringBuilder()

                        br.forEachLine { sb.append(it).append('\n') }

                        val content = sb.toString().replace("\r\n", "\n")

                        _htmCache[file.path.replace("\\", "/").hashCode()] = content
                        return content
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error caching HTM file.", e)
            return null
        }

    }

    /**
     * Check if an HTM exists and can be loaded. If so, it is loaded and stored.
     * @param path : The path to the HTM.
     * @return true if the HTM can be loaded.
     */
    fun isLoadable(path: String): Boolean {
        val file = File(path)
        return if (!_htmFilter.accept(file)) false else loadFile(file) != null

    }

    /**
     * Returns the HTM content given by filename. Test the cache first, then try to load the file if unsuccessful.
     * @param path : The path to the HTM.
     * @return the [String] content if filename exists, otherwise returns null.
     */
    fun getHtm(path: String): String {
        if (path.isEmpty())
            return ""

        var content: String? = _htmCache[path.hashCode()]
        if (content == null) {
            val file = File(path)
            if (_htmFilter.accept(file))
                content = loadFile(file)
        }

        return content ?: ""
    }

    /**
     * Return content of html message given by filename. In case filename does not exist, returns notice.
     * @param path : The path to the HTM.
     * @return the [String] content if filename exists, otherwise returns formatted default message.
     */
    fun getHtmForce(path: String): String {
        var content = getHtm(path)
        if (content.isEmpty()) {
            content = "<html><body>My html is missing:<br>$path</body></html>"
            LOGGER.warn("Following HTM {} is missing.", path)
        }

        return content
    }

    private class HtmFilter : FileFilter {
        override fun accept(file: File): Boolean {
            return file.isFile && (file.name.endsWith(".htm") || file.name.endsWith(".html"))
        }
    }
}