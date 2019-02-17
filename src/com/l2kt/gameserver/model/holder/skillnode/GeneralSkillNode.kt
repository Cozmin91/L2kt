package com.l2kt.gameserver.model.holder.skillnode

import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used by general skill types. It extends [SkillNode].
 */
open class GeneralSkillNode(set: StatsSet) : SkillNode(set) {
    val cost: Int = set.getInteger("cost")

    /**
     * Method used for Divine Inspiration skill implementation, since it uses -1 as cost (easier management). We couldn't keep 0, otherwise it would be considered as an autoGet and be freely given ; and using a boolean tag would kill me.<br></br>
     * <br></br>
     * **Only used to display the correct value to client, regular uses must be -1.**
     * @return 0 or the initial cost if superior to 0.
     */
    val correctedCost: Int
        get() = Math.max(0, cost)
}