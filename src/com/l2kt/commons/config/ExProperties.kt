package com.l2kt.commons.config

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

/**
 * @author G1ta0
 */
class ExProperties : Properties() {

    @Throws(IOException::class)
    fun load(fileName: String) {
        load(File(fileName))
    }

    @Throws(IOException::class)
    fun load(file: File) {
        FileInputStream(file).use { `is` -> load(`is`) }
    }

    fun getProperty(name: String, defaultValue: Boolean): Boolean {
        var `val` = defaultValue

        val value = super.getProperty(name, null)

        if (value != null)
            `val` = java.lang.Boolean.parseBoolean(value)

        return `val`
    }

    fun getProperty(name: String, defaultValue: Int): Int {
        var `val` = defaultValue

        val value = super.getProperty(name, null)

        if (value != null)
            `val` = Integer.parseInt(value)

        return `val`
    }

    fun getProperty(name: String, defaultValue: Long): Long {
        var `val` = defaultValue

        val value = super.getProperty(name, null)

        if (value != null)
            `val` = java.lang.Long.parseLong(value)

        return `val`
    }

    fun getProperty(name: String, defaultValue: Double): Double {
        var `val` = defaultValue

        val value = super.getProperty(name, null)

        if (value != null)
            `val` = java.lang.Double.parseDouble(value)

        return `val`
    }

    @JvmOverloads
    fun getProperty(name: String, defaultValue: Array<String>, delimiter: String = defaultDelimiter): Array<String>? {
        var `val` = defaultValue
        val value = super.getProperty(name, null)

        if (value != null)
            `val` = value.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        return `val`
    }

    @JvmOverloads
    fun getProperty(name: String, defaultValue: BooleanArray, delimiter: String = defaultDelimiter): BooleanArray {
        var `val` = defaultValue
        val value = super.getProperty(name, null)

        if (value != null) {
            val values = value.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            `val` = BooleanArray(values.size)
            for (i in `val`.indices)
                `val`[i] = java.lang.Boolean.parseBoolean(values[i])
        }

        return `val`
    }

    @JvmOverloads
    fun getProperty(name: String, defaultValue: IntArray, delimiter: String = defaultDelimiter): IntArray {
        var `val` = defaultValue
        val value = super.getProperty(name, null)

        if (value != null) {
            val values = value.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            `val` = IntArray(values.size)
            for (i in `val`.indices)
                `val`[i] = Integer.parseInt(values[i])
        }

        return `val`
    }

    @JvmOverloads
    fun getProperty(name: String, defaultValue: LongArray, delimiter: String = defaultDelimiter): LongArray {
        var `val` = defaultValue
        val value = super.getProperty(name, null)

        if (value != null) {
            val values = value.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            `val` = LongArray(values.size)
            for (i in `val`.indices)
                `val`[i] = java.lang.Long.parseLong(values[i])
        }

        return `val`
    }

    @JvmOverloads
    fun getProperty(name: String, defaultValue: DoubleArray, delimiter: String = defaultDelimiter): DoubleArray {
        var `val` = defaultValue
        val value= super.getProperty(name, null)

        if (value != null) {
            val values = value.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            `val` = DoubleArray(values.size)
            for (i in `val`.indices)
                `val`[i] = java.lang.Double.parseDouble(values[i])
        }

        return `val`
    }

    companion object {

        private val defaultDelimiter = "[\\s,;]+"
    }
}