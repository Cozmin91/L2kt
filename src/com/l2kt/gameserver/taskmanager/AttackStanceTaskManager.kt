package com.l2kt.gameserver.taskmanager

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Cubic
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.AutoAttackStop
import java.util.concurrent.ConcurrentHashMap

/**
 * Turns off attack stance of [Creature] after PERIOD ms.
 */
object AttackStanceTaskManager : Runnable {

    private val characters = ConcurrentHashMap<Creature, Long>()
    private const val ATTACK_STANCE_PERIOD_MS: Long = 15000

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

            character.broadcastPacket(AutoAttackStop(character.objectId))

            if (character is Player) {
                val summon = character.getPet()
                summon?.broadcastPacket(AutoAttackStop(summon.objectId))
            }

            characters.remove(character)
        }
    }

    fun add(character: Creature) {
        if (character is Playable) {
            for (cubic in character.actingPlayer!!.cubics.values)
                if (cubic.id != Cubic.LIFE_CUBIC)
                    cubic.doAction()
        }

        characters[character] = System.currentTimeMillis() + ATTACK_STANCE_PERIOD_MS
    }

    fun remove(character: Creature) {
        var character = character
        if (character is Summon)
            character = character.actingPlayer as Creature

        characters.remove(character)
    }

    /**
     * Tests if [Creature] is in AttackStanceTask.
     * @param character : [Creature] to be removed.
     * @return boolean : True when [Creature] is in attack stance.
     */
    fun isInAttackStance(character: Creature): Boolean {
        var character = character
        if (character is Summon)
            character = character.actingPlayer as Creature

        return characters.containsKey(character)
    }
}