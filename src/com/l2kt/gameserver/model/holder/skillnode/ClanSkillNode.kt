package com.l2kt.gameserver.model.holder.skillnode

import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used by clan skill types. It extends [GeneralSkillNode].
 */
class ClanSkillNode(set: StatsSet) : GeneralSkillNode(set) {
    val itemId: Int = set.getInteger("itemId")

}