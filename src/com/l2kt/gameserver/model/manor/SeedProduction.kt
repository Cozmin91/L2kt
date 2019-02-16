package com.l2kt.gameserver.model.manor

import java.util.concurrent.atomic.AtomicInteger

open class SeedProduction(val id: Int, amount: Int, val price: Int, val startAmount: Int) {
    private val _amount: AtomicInteger = AtomicInteger(amount)

    var amount: Int
        get() = _amount.get()
        set(amount) = _amount.set(amount)

    fun decreaseAmount(`val`: Int): Boolean {
        var current: Int
        var next: Int
        do {
            current = _amount.get()
            next = current - `val`

            if (next < 0)
                return false
        } while (!_amount.compareAndSet(current, next))

        return true
    }
}