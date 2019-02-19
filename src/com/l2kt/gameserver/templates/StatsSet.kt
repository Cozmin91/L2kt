package com.l2kt.gameserver.templates

import com.l2kt.gameserver.model.holder.IntIntHolder
import java.util.*
import java.util.stream.Stream

/**
 * This class is used in order to have a set of couples (key,value).<BR></BR>
 * Methods deployed are accessors to the set (add/get value from its key) and addition of a whole set in the current one.
 * @author mkizub, G1ta0
 */
open class StatsSet : HashMap<String, Any> {
    constructor() : super() {}

    constructor(size: Int) : super(size) {}

    constructor(set: StatsSet) : super(set) {}

    operator fun set(key: String, value: Any?) {
        if(value != null) put(key, value)
    }

    open operator fun set(key: String, value: String?) {
        if(value != null) put(key, value)
    }

    open operator fun set(key: String, value: Boolean) {
        put(key, if (value) java.lang.Boolean.TRUE else java.lang.Boolean.FALSE)
    }

    open operator fun set(key: String, value: Int?) {
        if(value != null) put(key, value)
    }

    operator fun set(key: String, value: IntArray) {
        put(key, value)
    }

    open operator fun set(key: String, value: Long?) {
        if(value != null) put(key, value)
    }

    open operator fun set(key: String, value: Double) {
        put(key, value)
    }

    open operator fun set(key: String, value: Enum<*>) {
        put(key, value)
    }

    fun unset(key: String) {
        remove(key)
    }

    fun getBool(key: String): Boolean {
        val `val` = get(key)

        if (`val` is Boolean)
            return `val`
        if (`val` is String)
            return java.lang.Boolean.parseBoolean(`val`)
        if (`val` is Number)
            return `val`.toInt() != 0

        throw IllegalArgumentException("StatsSet : Boolean value required, but found: $`val` for key: $key.")
    }

    fun getBool(key: String, defaultValue: Boolean): Boolean {
        val `val` = get(key)

        if (`val` is Boolean)
            return `val`
        if (`val` is String)
            return java.lang.Boolean.parseBoolean(`val`)
        return if (`val` is Number) `val`.toInt() != 0 else defaultValue

    }

    fun getByte(key: String): Byte {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toByte()
        if (`val` is String)
            return java.lang.Byte.parseByte(`val`)

        throw IllegalArgumentException("StatsSet : Byte value required, but found: $`val` for key: $key.")
    }

    fun getByte(key: String, defaultValue: Byte): Byte {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toByte()
        return if (`val` is String) java.lang.Byte.parseByte(`val`) else defaultValue

    }

    fun getDouble(key: String): Double {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toDouble()
        if (`val` is String)
            return java.lang.Double.parseDouble(`val`)
        if (`val` is Boolean)
            return if (`val`) 1.0 else 0.0

        throw IllegalArgumentException("StatsSet : Double value required, but found: $`val` for key: $key.")
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toDouble()
        if (`val` is String)
            return java.lang.Double.parseDouble(`val`)
        return if (`val` is Boolean) if (`val`) 1.0 else 0.0 else defaultValue

    }

    fun getDoubleArray(key: String): DoubleArray {
        val `val` = get(key)

        if (`val` is DoubleArray)
            return `val`
        if (`val` is Number)
            return doubleArrayOf(`val`.toDouble())
        if (`val` is String)
            return Stream.of(*`val`.split(";").dropLastWhile { it.isEmpty() }.toTypedArray())
                .mapToDouble { java.lang.Double.parseDouble(it) }.toArray()

        throw IllegalArgumentException("StatsSet : Double array required, but found: $`val` for key: $key.")
    }

    fun getFloat(key: String): Float {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toFloat()
        if (`val` is String)
            return java.lang.Float.parseFloat(`val`)
        if (`val` is Boolean)
            return (if (`val`) 1 else 0).toFloat()

        throw IllegalArgumentException("StatsSet : Float value required, but found: $`val` for key: $key.")
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toFloat()
        if (`val` is String)
            return java.lang.Float.parseFloat(`val`)
        return if (`val` is Boolean) (if (`val`) 1 else 0).toFloat() else defaultValue

    }

    open fun getInteger(key: String): Int {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toInt()
        if (`val` is String)
            return Integer.parseInt(`val`)
        if (`val` is Boolean)
            return if (`val`) 1 else 0

        throw IllegalArgumentException("StatsSet : Integer value required, but found: $`val` for key: $key.")
    }

    fun getInteger(key: String, defaultValue: Int): Int {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toInt()
        if (`val` is String)
            return Integer.parseInt(`val`)
        return if (`val` is Boolean) if (`val`) 1 else 0 else defaultValue

    }

    fun getIntegerArray(key: String): IntArray {
        val `val` = get(key)

        if (`val` is IntArray)
            return `val`
        if (`val` is Number)
            return intArrayOf(`val`.toInt())
        if (`val` is String)
            return Stream.of(*`val`.split(";").dropLastWhile { it.isEmpty() }.toTypedArray())
                .mapToInt { Integer.parseInt(it) }.toArray()

        throw IllegalArgumentException("StatsSet : Integer array required, but found: $`val` for key: $key.")
    }

    fun getIntegerArray(key: String, defaultArray: IntArray): IntArray {
        try {
            return getIntegerArray(key)
        } catch (e: IllegalArgumentException) {
            return defaultArray
        }

    }

    fun <T> getList(key: String): MutableList<T> {
        val `val` = get(key) ?: return mutableListOf()

        return `val` as MutableList<T>
    }

    fun getLong(key: String): Long {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toLong()
        if (`val` is String)
            return java.lang.Long.parseLong(`val`)
        if (`val` is Boolean)
            return if (`val`) 1L else 0L

        throw IllegalArgumentException("StatsSet : Long value required, but found: $`val` for key: $key.")
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val `val` = get(key)

        if (`val` is Number)
            return `val`.toLong()
        if (`val` is String)
            return java.lang.Long.parseLong(`val`)
        return if (`val` is Boolean) if (`val`) 1L else 0L else defaultValue

    }

    fun getLongArray(key: String): LongArray {
        val `val` = get(key)

        if (`val` is LongArray)
            return `val`
        if (`val` is Number)
            return longArrayOf(`val`.toLong())
        if (`val` is String)
            return Stream.of(*`val`.split(";").dropLastWhile { it.isEmpty() }.toTypedArray())
                .mapToLong { java.lang.Long.parseLong(it) }.toArray()

        throw IllegalArgumentException("StatsSet : Long array required, but found: $`val` for key: $key.")
    }

    fun <T, U> getMap(key: String): Map<T, U> {
        val `val` = get(key) ?: return emptyMap()

        return `val` as Map<T, U>
    }

    fun getString(key: String): String {
        val `val` = get(key)

        if (`val` != null)
            return `val`.toString()

        throw IllegalArgumentException("StatsSet : String value required, but unspecified for key: $key.")
    }

    fun getString(key: String, defaultValue: String?): String? {
        val `val` = get(key)

        return `val`?.toString() ?: defaultValue

    }

    fun getStringArray(key: String): Array<String> {
        val `val` = get(key)

        if (`val` is Array<*> && `val`.isArrayOf<String>())
            return `val` as Array<String>
        if (`val` is String)
            return `val`.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()

        throw IllegalArgumentException("StatsSet : String array required, but found: $`val` for key: $key.")
    }

    fun getIntIntHolder(key: String): IntIntHolder {
        val `val` = get(key)

        if (`val` is Array<*> && `val`.isArrayOf<String>()) {
            return IntIntHolder(Integer.parseInt(`val`[0] as String), Integer.parseInt(`val`[1] as String))
        }

        if (`val` is String) {
            val toSplit = `val`.split("-").dropLastWhile { it.isEmpty() }.toTypedArray()
            return IntIntHolder(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1]))
        }

        throw IllegalArgumentException("StatsSet : int-int (IntIntHolder) required, but found: $`val` for key: $key.")
    }

    fun getIntIntHolderList(key: String): List<IntIntHolder> {
        val `val` = get(key)

        if (`val` is String) {
            // String exists, but it empty : return a generic empty List.
            if (`val`.isEmpty())
                return emptyList()

            // Single entry ; return the entry under List form.
            if (!`val`.contains(";")) {
                val toSplit = `val`.split("-").dropLastWhile { it.isEmpty() }.toTypedArray()
                return Arrays.asList(IntIntHolder(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1])))
            }

            // First split is using ";", second is using "-". Exemple : 1234-12;1234-12.
            val entries = `val`.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()
            val list = ArrayList<IntIntHolder>(entries.size)

            // Feed the List.
            for (entry in entries) {
                val toSplit = entry.split("-").dropLastWhile { it.isEmpty() }.toTypedArray()
                list.add(IntIntHolder(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1])))
            }

            return list
        }

        throw IllegalArgumentException("StatsSet : int-int;int-int (List<IntIntHolder>) required, but found: $`val` for key: $key.")
    }

    fun <A> getObject(key: String, type: Class<A>): A? {
        val `val` = get(key)

        return if (`val` == null || !type.isAssignableFrom(`val`.javaClass)) null else `val` as A?

    }

    fun <E : Enum<E>> getEnum(name: String, enumClass: Class<E>): E {
        val `val` = get(name)

        if (`val` != null && enumClass.isInstance(`val`))
            return `val` as E
        if (`val` is String)
            return java.lang.Enum.valueOf<E>(enumClass, (`val` as String?))

        throw IllegalArgumentException("Enum value of type " + enumClass.name + "required, but found: " + `val` + ".")
    }

    fun <E : Enum<E>> getEnum(name: String, enumClass: Class<E>, defaultValue: E?): E? {
        val `val` = get(name)

        if (`val` != null && enumClass.isInstance(`val`))
            return `val` as E
        return if (`val` is String) java.lang.Enum.valueOf<E>(enumClass, (`val` as String?)) else defaultValue

    }
}