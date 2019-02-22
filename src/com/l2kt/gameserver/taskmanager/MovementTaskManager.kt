package com.l2kt.gameserver.taskmanager

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Updates position of moving [Creature] periodically.
 */
object MovementTaskManager : Runnable {

    private val characters = ConcurrentHashMap.newKeySet<Creature>()
    private const val MILLIS_PER_UPDATE = 100

    /**
     * @return the current number of ticks. Used as a monotonic clock wall with 100ms timelapse.
     */
    var ticks: Long = 0
        private set

    init {
        // Run task each 100 ms.
        ThreadPool.scheduleAtFixedRate(this, MILLIS_PER_UPDATE.toLong(), MILLIS_PER_UPDATE.toLong())
    }

    override fun run() {
        ticks++
        characters.forEach { character ->
            if (!character.updatePosition())
                return@forEach

            characters.remove(character)

            val ai = character.ai ?: return@forEach
            GlobalScope.launch {
                ai.notifyEvent(CtrlEvent.EVT_ARRIVED)
            }
        }
    }

    /**
     * Add a [Creature] to MovementTask in order to update its location every MILLIS_PER_UPDATE ms.
     * @param cha The Creature to add to movingObjects of GameTimeController
     */
    fun add(cha: Creature) {
        characters.add(cha)
    }
}