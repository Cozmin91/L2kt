package com.l2kt.gameserver.taskmanager

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.ai.type.WalkerAI
import com.l2kt.gameserver.model.actor.instance.Walker
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles [Walker] waiting state case, when they got a delay option on their WalkNode.
 */
object WalkerTaskManager : Runnable {
    private val walkers = ConcurrentHashMap<Walker, Long>()

    init {
        ThreadPool.scheduleAtFixedRate(this, 1000, 1000)
    }

    override fun run() {
        if (walkers.isEmpty())
            return

        val time = System.currentTimeMillis()
        for ((walker, value) in walkers) {
            if (time < value)
                continue

            (walker.ai as WalkerAI).moveToNextPoint()
            walkers.remove(walker)
        }
    }

    /**
     * Adds [Walker] to the WalkerTaskManager.
     * @param walker : Walker to be added.
     * @param delay : The delay to add.
     */
    fun add(walker: Walker, delay: Int) {
        walkers[walker] = System.currentTimeMillis() + delay
    }
}