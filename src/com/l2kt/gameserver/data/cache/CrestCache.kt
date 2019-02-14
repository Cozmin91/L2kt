package com.l2kt.gameserver.data.cache

import com.l2kt.commons.logging.CLogger
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.*

/**
 * A cache storing clan crests under .dds format.<br></br>
 * <br></br>
 * Size integrity checks are made on crest save, deletion, get and also during server first load.
 */
object CrestCache {

    private val crests = HashMap<Int, ByteArray>()
    private val ddsFilter = DdsFilter()
    private val LOGGER = CLogger(CrestCache::class.java.name)
    private const val CRESTS_DIR = "./data/crests/"

    enum class CrestType(val prefix: String, val size: Int) {
        PLEDGE("Crest_", 256),
        PLEDGE_LARGE("LargeCrest_", 2176),
        ALLY("AllyCrest_", 192)
    }

    init {
        load()
    }

    /**
     * Initial method used to load crests data and store it in server memory.<br></br>
     * <br></br>
     * If a file doesn't meet integrity checks requirements, it is simply deleted.
     */
    private fun load() {
        for (file in File(CRESTS_DIR).listFiles()) {
            val fileName = file.name

            // Invalid file type has been found ; delete it.
            if (!ddsFilter.accept(file)) {
                file.delete()

                LOGGER.warn("Invalid file {} has been deleted while loading crests.", fileName)
                continue
            }

            // Load data on byte array.
            var data: ByteArray = byteArrayOf()
            try {
                RandomAccessFile(file, "r").use { f ->
                    data = ByteArray(f.length().toInt())
                    f.readFully(data)
                }
            } catch (e: Exception) {
                LOGGER.error("Error loading crest file: {}.", e, fileName)
                continue
            }

            // Test each crest type.
            for (type in CrestType.values()) {
                // We found a matching crest type.
                if (fileName.startsWith(type.prefix)) {
                    // The data size isn't the required one, delete the file.
                    if (data.size != type.size) {
                        file.delete()

                        LOGGER.warn("The data for crest {} is invalid. The crest has been deleted.", fileName)
                        continue
                    }

                    // Feed the cache with crest id as key, and crest data as value.
                    crests[(fileName.substring(type.prefix.length, fileName.length - 4)).toInt()] = data
                    continue
                }
            }
        }

        LOGGER.info("Loaded {} crests.", crests.size)
    }

    /**
     * Cleans the crest cache, and reload it.
     */
    fun reload() {
        crests.clear()

        load()
    }

    /**
     * @param type : The [CrestType] to refer on. Size integrity check is made based on it.
     * @param id : The crest id data to retrieve.
     * @return a byte array or null if id wasn't found.
     */
    fun getCrest(type: CrestType, id: Int): ByteArray? {
        // get crest data
        val data = crests[id]

        // crest data is not required type, return
        return if (data == null || data.size != type.size) null else data

    }

    /**
     * Removes the crest from both memory and file system.
     * @param type : The [CrestType] to refer on. Size integrity check is made based on it.
     * @param id : The crest id to delete.
     */
    fun removeCrest(type: CrestType, id: Int) {
        // get crest data
        val data = crests[id]

        // crest data is not required type, return
        if (data == null || data.size != type.size)
            return

        // remove from cache
        crests.remove(id)

        // delete file
        val file = File(CRESTS_DIR + type.prefix + id + ".dds")
        if (!file.delete())
            LOGGER.warn("Error deleting crest file: {}.", file.name)
    }

    /**
     * Stores the crest as a physical file and in cache memory.
     * @param type : The [CrestType] used to register the crest. Crest name uses it.
     * @param id : The crest id to register this new crest.
     * @param data : The crest data to store.
     * @return true if the crest has been successfully saved, false otherwise.
     */
    fun saveCrest(type: CrestType, id: Int, data: ByteArray): Boolean {
        // Create the file.
        val file = File(CRESTS_DIR + type.prefix + id + ".dds")

        // Verify the data size integrity.
        if (data.size != type.size) {
            LOGGER.warn("The data for crest {} is invalid. Saving process is aborted.", file.name)
            return false
        }

        // Save the crest file with given data.
        try {
            FileOutputStream(file).use { out -> out.write(data) }
        } catch (e: Exception) {
            LOGGER.error("Error saving crest file: {}.", e, file.name)
            return false
        }

        // Feed the cache with crest data.
        crests[id] = data

        return true
    }

    private class DdsFilter : FileFilter {
        override fun accept(file: File): Boolean {
            val fileName = file.name

            return (fileName.startsWith("Crest_") || fileName.startsWith("LargeCrest_") || fileName.startsWith("AllyCrest_")) && fileName.endsWith(
                ".dds"
            )
        }
    }
}