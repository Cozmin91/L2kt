package com.l2kt.gameserver.model.item

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used to retain Henna infos. Hennas are called "dye" ingame, and enhance [Player] stats for a fee.<br></br>
 * You can draw up to 3 hennas (depending about your current class rank), but accumulated boni for a stat can't be higher than +5. There is no limit in reduction.
 */
class Henna(set: StatsSet) {
    val symbolId: Int = set.getInteger("symbolId")
    val dyeId: Int = set.getInteger("dyeId")
    val price: Int = set.getInteger("price")
    val int: Int = set.getInteger("INT")
    val str: Int = set.getInteger("STR")
    val con: Int = set.getInteger("CON")
    val men: Int = set.getInteger("MEN")
    val dex: Int = set.getInteger("DEX")
    val wit: Int = set.getInteger("WIT")
    private val _classes: IntArray = set.getIntegerArray("classes")

    /**
     * Seek if this [Henna] can be used by a [Player], based on his classId.
     * @param player : The Player to check.
     * @return true if this Henna owns the Player classId.
     */
    fun canBeUsedBy(player: Player): Boolean {
        return _classes.contains(player.classId.id)
    }

    companion object {

        val requiredDyeAmount: Int
            get() = 10
    }
}