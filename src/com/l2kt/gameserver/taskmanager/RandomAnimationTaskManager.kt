package com.l2kt.gameserver.taskmanager

import java.util.concurrent.ConcurrentHashMap

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention

/**
 * Handles [Npc] random social animation after specified time.
 */
object RandomAnimationTaskManager : Runnable {
    private val characters = ConcurrentHashMap<Npc, Long>()

    init {
        ThreadPool.scheduleAtFixedRate(this, 1000, 1000)
    }

    override fun run() {
        if (characters.isEmpty())
            return

        val time = System.currentTimeMillis()

        for ((character, value) in characters) {
            if (time < value)
                continue

            if (character.isMob) {
                // Cancel further animation timers until intention is changed to ACTIVE again.
                if (character.ai.desire.intention != CtrlIntention.ACTIVE) {
                    characters.remove(character)
                    continue
                }
            } else {
                if (!character.isInActiveRegion)
                // NPCs in inactive region don't run this task
                {
                    characters.remove(character)
                    continue
                }
            }

            if (!(character.isDead() || character.isStunned || character.isSleeping || character.isParalyzed))
                character.onRandomAnimation(Rnd.get(2, 3))

            val timer =
                if (character.isMob) Rnd.get(Config.MIN_MONSTER_ANIMATION, Config.MAX_MONSTER_ANIMATION) else Rnd.get(
                    Config.MIN_NPC_ANIMATION,
                    Config.MAX_NPC_ANIMATION
                )
            add(character, timer)
        }
    }

    /**
     * Adds [Npc] to the RandomAnimationTask with additional interval.
     * @param character : [Npc] to be added.
     * @param interval : Interval in seconds, after which the decay task is triggered.
     */
    fun add(character: Npc, interval: Int) {
        characters[character] = System.currentTimeMillis() + interval * 1000
    }
}