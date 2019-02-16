package com.l2kt.gameserver.model.memo

import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Player

/**
 * A basic implementation of [AbstractMemo] used for NPC. There is no restore/save options, it is mostly used for dynamic content.
 */
class NpcMemo : AbstractMemo() {
    override fun getInteger(key: String): Int {
        return super.getInteger(key, 0)
    }

    public override fun restoreMe(): Boolean {
        return true
    }

    public override fun storeMe(): Boolean {
        return true
    }

    /**
     * Gets the stored player.
     * @param name the name of the variable
     * @return the stored player or `null`
     */
    fun getPlayer(name: String): Player? {
        return getObject(name, Player::class.java)
    }

    /**
     * Gets the stored summon.
     * @param name the name of the variable
     * @return the stored summon or `null`
     */
    fun getSummon(name: String): Summon? {
        return getObject(name, Summon::class.java)
    }
}