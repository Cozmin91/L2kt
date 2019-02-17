package com.l2kt.gameserver.model.holder.skillnode

import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used by fishing (or common) skill types. It extends [SkillNode].
 */
class FishingSkillNode(set: StatsSet) : SkillNode(set) {
    val itemId: Int = set.getInteger("itemId")
    val itemCount: Int = set.getInteger("itemCount")
    val isDwarven: Boolean = set.getBool("isDwarven", false)

}