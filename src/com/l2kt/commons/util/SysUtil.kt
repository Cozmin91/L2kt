package com.l2kt.commons.util

object SysUtil {
    private const val MEBIOCTET = 1024 * 1024

    /**
     * @return the used amount of memory the JVM is using.
     */
    @JvmStatic
    val usedMemory: Long
        get() = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEBIOCTET

    /**
     * @return the maximum amount of memory the JVM can use.
     */
    @JvmStatic
    val maxMemory: Long
        get() = Runtime.getRuntime().maxMemory() / MEBIOCTET
}