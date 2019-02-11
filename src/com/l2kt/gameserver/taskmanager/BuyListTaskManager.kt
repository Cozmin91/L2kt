package com.l2kt.gameserver.taskmanager

import java.util.concurrent.ConcurrentHashMap

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.buylist.Product

/**
 * Handles individual [Product] restock timers.<br></br>
 * A timer is set, then on activation it restocks and releases it from the map. Additionally, some SQL action is done.
 */
object BuyListTaskManager : Runnable {
    private val products = ConcurrentHashMap<Product, Long>()

    init {
        ThreadPool.scheduleAtFixedRate(this, 1000, 1000)
    }

    override fun run() {
        if (products.isEmpty())
            return

        val time = System.currentTimeMillis()

        for ((product, value) in products) {
            if (time < value)
                continue

            product.count = product.maxCount
            product.delete()

            products.remove(product)
        }
    }

    /**
     * Adds a [Product] to the task. A product can't be added twice.
     * @param product : [Product] to be added.
     * @param interval : Interval in minutes, after which the task is triggered.
     */
    fun add(product: Product, interval: Long) {
        val newRestockTime = System.currentTimeMillis() + interval
        if ((products).putIfAbsent(product, newRestockTime) == null)
            product.save(newRestockTime)
    }

    /**
     * Test the timer : if already gone, reset the count without adding the [Product] to the task. A product can't be added twice.
     * @param product : [Product] to be added.
     * @param currentCount : the amount to set, if remaining time succeeds.
     * @param nextRestockTime : time in milliseconds.
     */
    fun test(product: Product, currentCount: Int, nextRestockTime: Long) {
        if (nextRestockTime - System.currentTimeMillis() > 0) {
            product.count = currentCount
            (products).putIfAbsent(product, nextRestockTime)
        } else {
            product.count = product.maxCount
            product.delete()
        }
    }
}