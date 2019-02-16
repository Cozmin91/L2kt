package com.l2kt.gameserver.model.memo

import com.l2kt.gameserver.templates.StatsSet

import java.util.concurrent.atomic.AtomicBoolean

/**
 * A [StatsSet] which overrides methods to prevent doing useless database operations if there is no changes since last edit (it uses an AtomicBoolean to keep edition tracking).<br></br>
 * <br></br>
 * It also has 2 abstract methods, named restoreMe() and storeMe().
 */
abstract class AbstractMemo : StatsSet() {
    private val _hasChanges = AtomicBoolean(false)

    override fun set(name: String, value: Boolean) {
        _hasChanges.compareAndSet(false, true)
        super.set(name, value)
    }

    override fun set(name: String, value: Double) {
        _hasChanges.compareAndSet(false, true)
        super.set(name, value)
    }

    override fun set(name: String, value: Enum<*>) {
        _hasChanges.compareAndSet(false, true)
        super.set(name, value)
    }

    override fun set(name: String, value: Int?) {
        _hasChanges.compareAndSet(false, true)
        super.set(name, value)
    }

    override fun set(name: String, value: Long?) {
        _hasChanges.compareAndSet(false, true)
        super.set(name, value)
    }

    override fun set(name: String, value: String?) {
        _hasChanges.compareAndSet(false, true)
        super.set(name, value)
    }

    /**
     * @return `true` if changes are made since last load/save.
     */
    fun hasChanges(): Boolean {
        return _hasChanges.get()
    }

    /**
     * Atomically sets the value to the given updated value if the current value `==` the expected value.
     * @param expect
     * @param update
     * @return `true` if successful. `false` return indicates that the actual value was not equal to the expected value.
     */
    fun compareAndSetChanges(expect: Boolean, update: Boolean): Boolean {
        return _hasChanges.compareAndSet(expect, update)
    }

    /**
     * Removes variable
     * @param name
     */
    override fun remove(name: String) {
        _hasChanges.compareAndSet(false, true)
        unset(name)
    }

    protected abstract fun restoreMe(): Boolean

    protected abstract fun storeMe(): Boolean
}