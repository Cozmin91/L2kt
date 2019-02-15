package com.l2kt.commons.util

import java.util.*

object ArraysUtil {
    @JvmField
    val EMPTY_INT_ARRAY = intArrayOf()

    /**
     * @param <T> : The Object type.
     * @param array : the array to look into.
     * @return `true` if the array is empty or null.
    </T> */
    @JvmStatic
    fun <T> isEmpty(array: Array<T>?): Boolean {
        return array == null || array.isEmpty()
    }

    /**
     * @param <T> : The Object type.ArraysUtil
     * @param array : the array to look into.
     * @param obj : the object to search for.
     * @return `true` if the array contains the object, `false` otherwise.
    </T> */
    @JvmStatic
    fun <T> contains(array: Array<T>?, obj: T): Boolean {
        if (array == null || array.isEmpty())
            return false

        for (element in array)
            if (element == obj)
                return true

        return false
    }

    /**
     * @param <T> : The Object type.
     * @param array1 : the array to look into.
     * @param array2 : the array to search for.
     * @return `true` if both arrays contains a similar value.
    </T> */
    @JvmStatic
    fun <T> contains(array1: Array<T>?, array2: Array<T>?): Boolean {
        if (array1 == null || array1.isEmpty())
            return false

        if (array2 == null || array2.isEmpty())
            return false

        for (element1 in array1) {
            for (element2 in array2)
                if (element2 == element1)
                    return true
        }
        return false
    }

    /**
     * @param array : the array to look into.
     * @param obj : the integer to search for.
     * @return `true` if the array contains the integer, `false` otherwise.
     */
    @JvmStatic
    fun contains(array: IntArray?, obj: Int): Boolean {
        if (array == null || array.isEmpty())
            return false

        for (element in array)
            if (element == obj)
                return true

        return false
    }

    /**
     * Concat two arrays of the same type into a single array.
     * @param <T> : The Object type.
     * @param first : The initial array used as recipient.
     * @param second : The second array to merge.
     * @return an array of the given type, holding informations of passed arrays.
    </T> */
    @JvmStatic
    fun <T> concat(first: Array<T>, second: Array<T>): Array<T> {
        val result = Arrays.copyOf(first, first.size + second.size)
        System.arraycopy(second, 0, result, first.size, second.size)
        return result
    }

    /**
     * Concat multiple arrays of the same type into a single array.
     * @param <T> : The Object type.
     * @param first : The initial array used as recipient.
     * @param rest : An array vararg.
     * @return an array of the given type, holding informations of passed arrays.
    </T> */
    @JvmStatic
    @SafeVarargs
    fun <T> concatAll(first: Array<T>, vararg rest: Array<T>): Array<T> {
        var totalLength = first.size
        for (array in rest)
            totalLength += array.size

        val result = Arrays.copyOf(first, totalLength)
        var offset = first.size
        for (array in rest) {
            System.arraycopy(array, 0, result, offset, array.size)
            offset += array.size
        }
        return result
    }
}