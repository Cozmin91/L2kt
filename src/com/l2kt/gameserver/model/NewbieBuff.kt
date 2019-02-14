package com.l2kt.gameserver.model

import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used only by Newbie Buffers NPC type. Those are beneficial magic effects launched on newbie players in order to help them in their Lineage 2 adventures.<br></br>
 * <br></br>
 * Those buffs got level limitation, and are class based (fighter or mage type).
 */
class NewbieBuff(set: StatsSet) {
    /**
     * @return the lower level that the player must achieve in order to obtain this buff.
     */
    val lowerLevel: Int = set.getInteger("lowerLevel")
    /**
     * @return the upper level that the player mustn't exceed in order to obtain this buff.
     */
    val upperLevel: Int = set.getInteger("upperLevel")
    /**
     * @return the skill id of the buff that the player will receive.
     */
    val skillId: Int = set.getInteger("skillId")
    /**
     * @return the level of the buff that the player will receive.
     */
    val skillLevel: Int = set.getInteger("skillLevel")
    /**
     * @return false if it's a fighter buff, true if it's a magic buff.
     */
    val isMagicClassBuff: Boolean = set.getBool("isMagicClass")

}