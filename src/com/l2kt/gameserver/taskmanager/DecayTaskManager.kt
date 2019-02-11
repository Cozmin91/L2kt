package com.l2kt.gameserver.taskmanager

import java.util.concurrent.ConcurrentHashMap

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature

/**
 * Destroys [Creature] corpse after specified time.
 */
object DecayTaskManager : Runnable {
    private val characters = ConcurrentHashMap<Creature, Long>()

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

            character.onDecay()
            characters.remove(character)
        }
    }

    /**
     * Adds [Creature] to the DecayTask with additional interval.
     * @param character : [Creature] to be added.
     * @param interval : Interval in seconds, after which the decay task is triggered.
     */
    fun add(character: Creature, interval: Int) {
        var interval = interval
        if (character is Attackable) {

            // monster is spoiled or seeded, double the corpse delay
            if (character.spoilerId != 0 || character.isSeeded)
                interval *= 2
        }

        characters[character] = System.currentTimeMillis() + interval * 1000
    }

    fun cancel(actor: Creature) {
        characters.remove(actor)
    }

    /**
     * Removes [Attackable] from the DecayTask.
     * @param monster : [Attackable] to be tested.
     * @return boolean : True, when action can be applied on a corpse.
     */
    fun isCorpseActionAllowed(monster: Attackable): Boolean {
        val time = characters[monster] ?: return false
        var corpseTime = monster.template.corpseTime * 1000 / 2

        if (monster.spoilerId != 0 || monster.isSeeded)
            corpseTime *= 2

        return System.currentTimeMillis() < time - corpseTime
    }
}