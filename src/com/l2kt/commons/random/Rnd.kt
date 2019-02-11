package com.l2kt.commons.random

import java.util.concurrent.ThreadLocalRandom

/**
 * A central randomness provider. Currently all methods delegate to [ThreadLocalRandom].
 * @author _dev_
 */
object Rnd {
    fun nextDouble(): Double {
        return ThreadLocalRandom.current().nextDouble()
    }

    fun nextInt(n: Int): Int {
        return ThreadLocalRandom.current().nextInt(n)
    }

    fun nextInt(): Int {
        return ThreadLocalRandom.current().nextInt()
    }

    operator fun get(n: Int): Int {
        return nextInt(n)
    }

    operator fun get(min: Int, max: Int): Int {
        return ThreadLocalRandom.current().nextInt(min, if (max == Integer.MAX_VALUE) max else max + 1)
    }

    fun nextLong(n: Long): Long {
        return ThreadLocalRandom.current().nextLong(n)
    }

    fun nextLong(): Long {
        return ThreadLocalRandom.current().nextLong()
    }

    operator fun get(n: Long): Long {
        return nextLong(n)
    }

    operator fun get(min: Long, max: Long): Long {
        return ThreadLocalRandom.current().nextLong(min, if (max == java.lang.Long.MAX_VALUE) max else max + 1L)
    }

    fun calcChance(applicableUnits: Double, totalUnits: Int): Boolean {
        return applicableUnits > nextInt(totalUnits)
    }

    fun nextGaussian(): Double {
        return ThreadLocalRandom.current().nextGaussian()
    }

    fun nextBoolean(): Boolean {
        return ThreadLocalRandom.current().nextBoolean()
    }

    fun nextBytes(count: Int): ByteArray {
        return nextBytes(ByteArray(count))
    }

    fun nextBytes(array: ByteArray): ByteArray {
        ThreadLocalRandom.current().nextBytes(array)
        return array
    }

    /**
     * Returns a randomly selected element taken from the given list.
     * @param <T> type of list elements.
     * @param list a list.
     * @return a randomly selected element.
    </T> */
    operator fun <T> get(list: List<T>?): T? {
        return if (list == null || list.isEmpty()) null else list[get(list.size)]
    }

    /**
     * Returns a randomly selected element taken from the given array.
     * @param <T> type of array elements.
     * @param array an array.
     * @return a randomly selected element.
    </T> */
    operator fun <T> get(array: Array<T>?): T? {
        return if (array == null || array.isEmpty()) null else array[get(array.size)]
    }
}